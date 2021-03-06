/*
 * Copyright 2015-2016 OpenCB
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gel.cva.server.rest;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.base.Splitter;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.avro.generic.GenericRecord;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;
import org.gel.cva.storage.core.config.CvaConfiguration;
import org.gel.cva.storage.core.exceptions.CvaException;
import org.gel.cva.storage.core.exceptions.IllegalCvaConfigurationException;
import org.gel.cva.storage.core.manager.KnownVariantManager;
import org.opencb.commons.datastore.core.*;
import org.opencb.opencga.core.auth.IllegalOpenCGACredentialsException;
import org.opencb.opencga.storage.core.config.StorageConfiguration;
import org.opencb.opencga.storage.core.variant.io.json.mixin.GenericRecordAvroJsonMixin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@ApplicationPath("/")
@Path("/{version}")
@Produces(MediaType.APPLICATION_JSON)
public class CvaWSServer {

    @DefaultValue("v1")
    @PathParam("version")
    @ApiParam(name = "version", value = "CVA major version", allowableValues = "v1", defaultValue = "v1")
    protected String version;

    //    @DefaultValue("")
//    @QueryParam("exclude")
//    @ApiParam(name = "exclude", value = "Fields excluded in response. Whole JSON path.")
    protected String exclude;

    //    @DefaultValue("")
//    @QueryParam("include")
//    @ApiParam(name = "include", value = "Only fields included in response. Whole JSON path.")
    protected String include;

    //    @DefaultValue("-1")
//    @QueryParam("limit")
//    @ApiParam(name = "limit", value = "Maximum number of documents to be returned.")
    protected int limit;

    //    @DefaultValue("0")
//    @QueryParam("skip")
//    @ApiParam(name = "skip", value = "Number of documents to be skipped when querying for data.")
    protected long skip;

    protected boolean count;
    protected boolean lazy;

    @DefaultValue("")
    @QueryParam("sid")
    @ApiParam(value = "Session Id")
    protected String sessionId;

    protected UriInfo uriInfo;
    protected HttpServletRequest httpServletRequest;
    protected MultivaluedMap<String, String> params;

    protected String sessionIp;

    protected long startTime;

    protected Query query;
    protected QueryOptions queryOptions;

    protected static ObjectWriter jsonObjectWriter;
    protected static ObjectMapper jsonObjectMapper;

    protected static Logger logger; // = LoggerFactory.getLogger(this.getClass());

//    @DefaultValue("true")
//    @QueryParam("metadata")
//    protected boolean metadata;


    protected static AtomicBoolean initialized;

    protected static CvaConfiguration configuration;
    protected static KnownVariantManager knownVariantManager;
    protected static StorageConfiguration storageConfiguration;

    private static final int DEFAULT_LIMIT = 2000;
    private static final int MAX_LIMIT = 5000;

    static {
        initialized = new AtomicBoolean(false);

        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.addMixIn(GenericRecord.class, GenericRecordAvroJsonMixin.class);
        jsonObjectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        jsonObjectMapper.configure(MapperFeature.REQUIRE_SETTERS_FOR_GETTERS, true);
        jsonObjectWriter = jsonObjectMapper.writer();
        
        //Disable MongoDB useless logging
        org.apache.log4j.Logger.getLogger("org.mongodb.driver.cluster").setLevel(Level.WARN);
        org.apache.log4j.Logger.getLogger("org.mongodb.driver.connection").setLevel(Level.WARN);
    }


    public CvaWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest) throws IOException {
        this(uriInfo.getPathParameters().getFirst("version"), uriInfo, httpServletRequest);
    }

    public CvaWSServer(@PathParam("version") String version, @Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest)
            throws IOException {
        this.version = version;
        this.uriInfo = uriInfo;
        this.httpServletRequest = httpServletRequest;

        this.params = uriInfo.getQueryParameters();

        // This is only executed the first time to initialize configuration and some variables
        if (initialized.compareAndSet(false, true)) {
            init();
        }

        query = new Query();
        queryOptions = new QueryOptions();

        parseParams();

        // take the time for calculating the whole duration of the call
        startTime = System.currentTimeMillis();
    }

    private void init() {
        logger = LoggerFactory.getLogger("org.gel.cva.server.rest.CvaWSServer");
        logger.info("========================================================================");
        logger.info("| Starting CVA REST server, initializing CvaWSServer");
        logger.info("| This message must appear only once.");

        // We must load the configuration files and init knownVariantManager and Logger only the first time.
        // We first read 'config-dir' parameter passed
        ServletContext context = httpServletRequest.getServletContext();
        try {
            configuration = CvaConfiguration.getInstance();
            knownVariantManager = new KnownVariantManager(configuration);
        } catch (IllegalCvaConfigurationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }

        /*
        this.initCvaObjects(configDirString);
        if (StringUtils.isEmpty(configDirString)) {
            // If not environment variable then we check web.xml parameter
            if (StringUtils.isNotEmpty(context.getInitParameter("CVA_HOME"))) {
                configDirString = context.getInitParameter("CVA_HOME") + "/conf";
            } else if (StringUtils.isNotEmpty(System.getenv("CVA_HOME"))) {
                // If not exists then we try the environment variable CVA_HOME
                configDirString = System.getenv("CVA_HOME") + "/conf";
            } else {
                logger.error("No valid configuration directory provided!");
            }
        }
        */

        logger.info("========================================================================\n");
    }

    /**
     * This method loads OpenCGA configuration files and initialize CatalogManager and StorageManagerFactory.
     * This must be only executed once.
     * @param configDir directory containing the configuration files
     */
    private void initCvaObjects(java.nio.file.Path configDir) {
        try {
            logger.info("|  * Catalog configuration file: '{}'", configDir.toFile().getAbsolutePath() + "/cva.yml");
            configuration = CvaConfiguration
                    .load(new FileInputStream(new File(configDir.toFile().getAbsolutePath() + "/cva.yml")), "yaml");
            knownVariantManager = new KnownVariantManager(configuration);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CvaException e) {
            logger.error("Error while creating KnownVariantManager", e);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalOpenCGACredentialsException e) {
            e.printStackTrace();
        }
    }

    private void initLogger(java.nio.file.Path logs) {
        try {
            org.apache.log4j.Logger rootLogger = LogManager.getRootLogger();
            PatternLayout layout = new PatternLayout("%d{yyyy-MM-dd HH:mm:ss} [%t] %-5p %c{1}:%L - %m%n");
            String logFile = logs.resolve("server.log").toString();
            RollingFileAppender rollingFileAppender = new RollingFileAppender(layout, logFile, true);
            rollingFileAppender.setThreshold(Level.DEBUG);
            rollingFileAppender.setMaxFileSize("20MB");
            rollingFileAppender.setMaxBackupIndex(10);
            rootLogger.setLevel(Level.TRACE);
            rootLogger.addAppender(rollingFileAppender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//
//    /**
//     * Builds the query and the queryOptions based on the query parameters.
//     *
//     * @param params Map of parameters.
//     * @param getParam Method that returns the QueryParams object based on the key.
//     * @param query Query where parameters parsing the getParam function will be inserted.
//     * @param queryOptions QueryOptions where parameters not parsing the getParam function will be inserted.
//     */
//    @Deprecated
//    protected static void parseQueryParams(Map<String, List<String>> params,
//                                           Function<String, org.opencb.commons.datastore.core.QueryParam> getParam,
//                                           ObjectMap query, QueryOptions queryOptions) {
//        for (Map.Entry<String, List<String>> entry : params.entrySet()) {
//            String param = entry.getKey();
//            int indexOf = param.indexOf('.');
//            param = indexOf > 0 ? param.substring(0, indexOf) : param;
//
//            if (getParam.apply(param) != null) {
//                query.put(entry.getKey(), entry.getValue().get(0));
//            } else {
//                queryOptions.add(param, entry.getValue().get(0));
//            }
//
//            // Exceptions
//            if (param.equalsIgnoreCase("status")) {
//                query.put("status.name", entry.getValue().get(0));
//                query.remove("status");
//                queryOptions.remove("status");
//            }
//
//            if (param.equalsIgnoreCase("jobId")) {
//                query.put("job.id", entry.getValue().get(0));
//                query.remove("jobId");
//                queryOptions.remove("jobId");
//            }
//
//            if (param.equalsIgnoreCase("individualId")) {
//                query.put("individual.id", entry.getValue().get(0));
//                query.remove("individualId");
//                queryOptions.remove("individualId");
//            }
//
//            if (param.equalsIgnoreCase("sid")) {
//                query.remove("sid");
//                queryOptions.remove("sid");
//            }
//        }
//        logger.debug("parseQueryParams: Query {}, queryOptions {}", query.safeToString(), queryOptions.safeToString());
//    }

    private void parseParams()  {
        // If by any reason 'version' is null we try to read it from the URI path, if not present an Exception is thrown
        if (version == null) {
            if (uriInfo.getPathParameters().containsKey("version")) {
                logger.warn("Setting 'version' from UriInfo object");
                this.version = uriInfo.getPathParameters().getFirst("version");
            } else {
                //TODO: throw new Exception("Version not valid: '" + version + "'");
            }
        }

        // Check version parameter, must be: v1, v2, ... If 'latest' then is converted to appropriate version.
        if (version.equalsIgnoreCase("latest")) {
            logger.info("Version 'latest' detected, setting 'version' parameter to 'v1'");
            version = "v1";
        }

        MultivaluedMap<String, String> multivaluedMap = uriInfo.getQueryParameters();
        queryOptions.put("metadata", multivaluedMap.get("metadata") == null || multivaluedMap.get("metadata").get(0).equals("true"));

        // Add all the others QueryParams from the URL
        for (Map.Entry<String, List<String>> entry : multivaluedMap.entrySet()) {
            String value =  entry.getValue().get(0);
            switch (entry.getKey()) {
                case QueryOptions.INCLUDE:
                case QueryOptions.EXCLUDE:
                case QueryOptions.SORT:
                    queryOptions.put(entry.getKey(), new LinkedList<>(Splitter.on(",").splitToList(value)));
                    break;
                case QueryOptions.LIMIT:
                    limit = Integer.parseInt(value);
                    break;
                case QueryOptions.SKIP:
                    int skip = Integer.parseInt(value);
                    queryOptions.put(entry.getKey(), (skip >= 0) ? skip : -1);
                    break;
                case QueryOptions.ORDER:
                    queryOptions.put(entry.getKey(), value);
                    break;
                case "count":
                case "lazy":
                    boolean booleanValue = Boolean.parseBoolean(value);
                    queryOptions.put(entry.getKey(), booleanValue);
                    break;
                default:
                    // Query
                    query.put(entry.getKey(), value);
                    break;
            }
        }

        queryOptions.put(QueryOptions.LIMIT, (limit > 0) ? Math.min(limit, MAX_LIMIT) : DEFAULT_LIMIT);
        query.remove("sid");

//      Exceptions
        if (query.containsKey("status")) {
            query.put("status.name", query.get("status"));
            query.remove("status");
        }

        try {
            Marker marker = MarkerFactory.getMarker("URL: {}, query = {}, queryOptions = {}");
            logger.info(marker, uriInfo.getAbsolutePath().toString(),
                    jsonObjectWriter.writeValueAsString(query), jsonObjectWriter.writeValueAsString(queryOptions));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    private void parseIncludeExclude(MultivaluedMap<String, String> multivaluedMap, String key, String value) {
        if(value != null && !value.isEmpty()) {
            queryOptions.put(key, new LinkedList<>(Splitter.on(",").splitToList(value)));
        } else {
            queryOptions.put(key, (multivaluedMap.get(key) != null)
                    ? Splitter.on(",").splitToList(multivaluedMap.get(key).get(0))
                    : null);
        }
    }


    protected void addParamIfNotNull(Map<String, String> params, String key, String value) {
        if (key != null && value != null) {
            params.put(key, value);
        }
    }

    protected void addParamIfTrue(Map<String, String> params, String key, boolean value) {
        if (key != null && value) {
            params.put(key, Boolean.toString(value));
        }
    }

    @Deprecated
    @GET
    @Path("/help")
    @ApiOperation(value = "Help", position = 1)
    public Response help() {
        return createOkResponse("No help available");
    }

    protected Response createErrorResponse(Exception e) {
        // First we print the exception in Server logs
        logger.error("Catch error: " + e.getMessage(), e);

        // Now we prepare the response to client
        QueryResponse<ObjectMap> queryResponse = new QueryResponse<>();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);
        if (StringUtils.isEmpty(e.getMessage())) {
            queryResponse.setError(e.toString());
        } else {
            queryResponse.setError(e.getMessage());
        }

        QueryResult<ObjectMap> result = new QueryResult<>();
        result.setWarningMsg("Future errors will ONLY be shown in the QueryResponse body");
        result.setErrorMsg("DEPRECATED: " + e.toString());
        queryResponse.setResponse(Arrays.asList(result));

        return Response.fromResponse(createJsonResponse(queryResponse))
                .status(Response.Status.INTERNAL_SERVER_ERROR).build();
//        return createOkResponse(result);
    }

//    protected Response createErrorResponse(String o) {
//        QueryResult<ObjectMap> result = new QueryResult();
//        result.setErrorMsg(o.toString());
//        return createOkResponse(result);
//    }

    protected Response createErrorResponse(String method, String errorMessage) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(new ObjectMap("error", errorMessage)), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return buildResponse(Response.ok("{\"error\":\"Error parsing json error\"}", MediaType.APPLICATION_JSON_TYPE));
    }

    // TODO: Change signature
    //    protected <T> Response createOkResponse(QueryResult<T> result)
    //    protected <T> Response createOkResponse(List<QueryResult<T>> results)
    protected Response createOkResponse(Object obj) {
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setTime(new Long(System.currentTimeMillis() - startTime).intValue());
        queryResponse.setApiVersion(version);
        queryResponse.setQueryOptions(queryOptions);

        // Guarantee that the QueryResponse object contains a list of results
        List list;
        if (obj instanceof List) {
            list = (List) obj;
        } else {
            list = new ArrayList();
            if (!(obj instanceof QueryResult)) {
                list.add(new QueryResult<>("", 0, 1, 1, "", "", Collections.singletonList(obj)));
            } else {
                list.add(obj);
            }
        }
        queryResponse.setResponse(list);

        return createJsonResponse(queryResponse);
    }

    //Response methods
    protected Response createOkResponse(Object o1, MediaType o2) {
        return buildResponse(Response.ok(o1, o2));
    }

    protected Response createOkResponse(Object o1, MediaType o2, String fileName) {
        return buildResponse(Response.ok(o1, o2).header("content-disposition", "attachment; filename =" + fileName));
    }


    protected Response createJsonResponse(QueryResponse queryResponse) {
        try {
            return buildResponse(Response.ok(jsonObjectWriter.writeValueAsString(queryResponse), MediaType.APPLICATION_JSON_TYPE));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            logger.error("Error parsing queryResponse object");
            return createErrorResponse("", "Error parsing QueryResponse object:\n" + Arrays.toString(e.getStackTrace()));
        }
    }

    protected Response buildResponse(Response.ResponseBuilder responseBuilder) {
        return responseBuilder
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Headers", "x-requested-with, content-type")
                .header("Access-Control-Allow-Credentials", "true")
                .header("Access-Control-Allow-Methods", "GET, POST, OPTIONS")
                .build();
    }

}
