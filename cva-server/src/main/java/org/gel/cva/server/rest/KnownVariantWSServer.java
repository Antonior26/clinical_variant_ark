/*
 * Copyright 2016-2017 Genomics England Limited
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

import io.swagger.annotations.*;
import org.gel.cva.storage.core.knownvariant.wrappers.KnownVariantWrapper;
import org.gel.models.cva.avro.*;
import org.gel.models.report.avro.EthnicCategory;
import org.gel.models.report.avro.ReportedModeOfInheritance;
import org.opencb.commons.datastore.core.QueryResult;
import org.opencb.opencga.core.exception.VersionException;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@Path("/{version}/variants")
@Produces(MediaType.APPLICATION_JSON)
@Api(value = "Known variants", position = 1, description = "Methods for working with 'variants' endpoint")
public class KnownVariantWSServer extends CvaWSServer {

    public KnownVariantWSServer(@Context UriInfo uriInfo, @Context HttpServletRequest httpServletRequest) throws IOException, VersionException {
        super(uriInfo, httpServletRequest);
    }

    @POST
    @Path("/insert")
    @ApiOperation(value = "Insert a known variant", position = 1,
            notes = "Inserts a known variant. <br>"
                    + "Required values: [submitter, chromosome, position, reference, alternate] <br>" +
                    "Chromosome name normalization, left alignment and trimming is applied before storing the variant. <br>" +
                    "Multi-allelic variants are not supported. <br>",
            response = KnownVariantWrapper.class)
    public Response insertKnownVariant(
            @ApiParam(value = "submitter", required = true)
            @QueryParam("submitter") String submitter,
            @ApiParam(value = "chromosome (e.g.: 1 or chr1 or chrom1 supported)", required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = "position (1-based)", required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = "reference", required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = "alternate", required = true)
            @QueryParam("alternate") String alternate) {

        try {
            KnownVariantWrapper result = knownVariantManager.createKnownVariant(
                    submitter,
                    chromosome,
                    position,
                    reference,
                    alternate
            );
            List<KnownVariant> results = new LinkedList<>();
            results.add(result.getImpl());
            QueryResult queryResult = new QueryResult<KnownVariant>(
                    "id",
                    0,
                    1,
                    1l,
                    "",
                    "",
                    results
            );
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @POST
    @Path("/addCuration")
    @ApiOperation(value = "Adds a curation to a known variant", position = 1,
            notes = "Adds a curation to a known variant. <br>"
                    + "Required values: [curator, chromosome, position, reference, alternate, phenotype, " +
                    "curationClassification] <br>" +
                    "Chromosome name normalization, left alignment and trimming is applied before searching for the variant. <br>",
            response = KnownVariantWrapper.class)
    public Response addCuration(
            @ApiParam(value = "curator", required = true)
            @QueryParam("curator") String curator,
            @ApiParam(value = "chromosome (e.g.: 1 or chr1 or chrom1 supported)", required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = "position (1-based)", required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = "reference", required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = "alternate", required = true)
            @QueryParam("alternate") String alternate,
            @ApiParam(value = "phenotype", required = true)
            @QueryParam("phenotype") String phenotype,
            @ApiParam(value = "modeOfInheritance")
            @QueryParam("modeOfInheritance") ReportedModeOfInheritance modeOfInheritance,
            @ApiParam(value = "transcript")
            @QueryParam("transcript") String transcript,
            @ApiParam(value = "curationClassification", required = true)
            @QueryParam("curationClassification") CurationClassification curationClassification,
            @ApiParam(value = "manualCurationConfidence")
            @QueryParam("manualCurationConfidence") ManualCurationConfidence manualCurationConfidence,
            @ApiParam(value = "consistencyStatus")
            @QueryParam("consistencyStatus") ConsistencyStatus consistencyStatus,
            @ApiParam(value = "penetrance")
            @QueryParam("penetrance") Float penetrance,
            @ApiParam(value = "variableExpressivity")
            @QueryParam("variableExpressivity") Boolean variableExpressivity
            )
    {

        try {
            KnownVariantWrapper result = knownVariantManager.addCuration(
                    chromosome,
                    position,
                    reference,
                    alternate,
                    curator,
                    phenotype,
                    modeOfInheritance,
                    transcript,
                    curationClassification,
                    manualCurationConfidence,
                    consistencyStatus,
                    penetrance,
                    variableExpressivity
            );
            List<KnownVariant> results = new LinkedList<>();
            results.add(result.getImpl());
            QueryResult queryResult = new QueryResult<KnownVariant>(
                    "id",
                    0,
                    1,
                    1l,
                    "",
                    "",
                    results
            );
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @POST
    @Path("/addEvidence")
    @ApiOperation(value = "Adds an evidence to a known variant", position = 1,
            notes = "Adds an evidence to a known variant. <br>"
                    + "Required values: [submitter, chromosome, position, reference, alternate] <br>" +
                    "Chromosome name normalization, left alignment and trimming is applied before searching for the variant. <br>",
            response = KnownVariantWrapper.class)
    public Response addEvidence(
            @ApiParam(value = "submitter", required = true)
            @QueryParam("submitter") String submitter,
            @ApiParam(value = "chromosome (e.g.: 1 or chr1 or chrom1 supported)", required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = "position (1-based)", required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = "reference", required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = "alternate", required = true)
            @QueryParam("alternate") String alternate,
            @ApiParam(value = "sourceName")
            @QueryParam("sourceName") String sourceName,
            @ApiParam(value = "sourceType", required = true)
            @QueryParam("sourceType") SourceType sourceType,
            @ApiParam(value = "sourceVersion")
            @QueryParam("sourceVersion") String sourceVersion,
            @ApiParam(value = "sourceUrl")
            @QueryParam("sourceUrl") String sourceUrl,
            @ApiParam(value = "sourceId")
            @QueryParam("sourceId") String sourceId,
            @ApiParam(value = "alleleOrigin")
            @QueryParam("alleleOrigin") AlleleOrigin alleleOrigin,
            @ApiParam(value = "phenotype")
            @QueryParam("phenotype") String phenotype,
            @ApiParam(value = "modeOfInheritance")
            @QueryParam("modeOfInheritance") ReportedModeOfInheritance modeOfInheritance,
            @ApiParam(value = "transcript")
            @QueryParam("transcript") String transcript,
            @ApiParam(value = "evidencePathogenicity")
            @QueryParam("evidencePathogenicity") EvidencePathogenicity evidencePathogenicity,
            @ApiParam(value = "evidenceBenignity")
            @QueryParam("evidenceBenignity") EvidenceBenignity evidenceBenignity,
            @ApiParam(value = "pubmedId")
            @QueryParam("pubmedId") String pubmedId,
            @ApiParam(value = "study")
            @QueryParam("study") String study,
            @ApiParam(value = "numberOfIndividuals")
            @QueryParam("numberOfIndividuals") Integer numberOfIndividuals,
            @ApiParam(value = "ethnicCategory")
            @QueryParam("ethnicCategory") EthnicCategory ethnicCategory,
            @ApiParam(value = "description")
            @QueryParam("description") String description
            )
    {

        HeritablePhenotype heritablePhenotype = new HeritablePhenotype(phenotype, modeOfInheritance);
        List<HeritablePhenotype> heritablePhenotypes = new LinkedList<>();
        heritablePhenotypes.add(heritablePhenotype);
        try {
            KnownVariantWrapper result = knownVariantManager.addEvidence(
                    chromosome,
                    position,
                    reference,
                    alternate,
                    submitter,
                    sourceName,
                    sourceType,
                    sourceVersion,
                    sourceUrl,
                    sourceId,
                    alleleOrigin,
                    heritablePhenotypes,
                    transcript,
                    evidencePathogenicity,
                    evidenceBenignity,
                    pubmedId,
                    study,
                    numberOfIndividuals,
                    ethnicCategory,
                    description
            );
            List<KnownVariant> results = new LinkedList<>();
            results.add(result.getImpl());
            QueryResult queryResult = new QueryResult<KnownVariant>(
                    "id",
                    0,
                    1,
                    1l,
                    "",
                    "",
                    results
            );
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }

    @GET
    @Path("/search")
    @ApiOperation(value = "Search for a known variant", position = 1,
            notes = "Search a known variant. <br>"
                    + "Required values: [chromosome, position, reference, alternate] <br>" +
                    "Chromosome name normalization, left alignment and trimming is applied before search. <br>" +
                    "TODO: search by position",
            response = KnownVariantWrapper.class)
    public Response searchKnownVariant(

            @ApiParam(value = "chromosome (e.g.: 1 or chr1 or chrom1 supported)", required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = "position (1-based)", required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = "reference", required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = "alternate", required = true)
            @QueryParam("alternate") String alternate) {

        try {
            KnownVariantWrapper result = knownVariantManager.findKnownVariant(
                    chromosome,
                    position,
                    reference,
                    alternate
            );
            List<KnownVariant> results = new LinkedList<>();
            results.add(result.getImpl());
            QueryResult queryResult = new QueryResult<KnownVariant>(
                    "id",
                    0,
                    1,
                    1l,
                    "",
                    "",
                    results
            );
            return createOkResponse(queryResult);
        } catch (Exception e) {
            return createErrorResponse(e);
        }
    }
}