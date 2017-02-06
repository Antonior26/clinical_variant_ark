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

    private static final String CHROMOSOME_API_PARAM = "Chromosome where the genomic variation occurred (e.g.: 1 or " +
            "chr1 or chrom1 supported)";
    private static final String POSITION_API_PARAM = "Variant's position (1-based)";
    private static final String REFERENCE_API_PARAM = "Variant's reference bases";
    private static final String ALTERNATE_API_PARAM = "Variant's alternate bases (multiple alleles not supported)";
    private static final String SUBMITTER_API_PARAM = "The variant's submitter (should be gone once user management " +
            "works)";
    private static final String PHENOTYPE_API_PARAM =  "The phenotype to which the curation refers (e.g.: HPO term, " +
            "MIM term, DO term etc.)";
    private static final String INHERITANCE_API_PARAM =  "The mode of inheritance." +
            "`monoallelic_not_imprinted`: MONOALLELIC, autosomal or pseudoautosomal, not imprinted <br>" +
            "`monoallelic_maternally_imprinted`: MONOALLELIC, autosomal or pseudoautosomal, " +
            "maternally imprinted (paternal allele expressed) <br>" +
            "`monoallelic_paternally_imprinted`: MONOALLELIC, autosomal or pseudoautosomal, paternally " +
            "imprinted (maternal allele expressed) <br>" +
            "`monoallelic`: MONOALLELIC, autosomal or pseudoautosomal, imprinted status unknown <br>" +
            "`biallelic`: BIALLELIC, autosomal or pseudoautosomal <br>" +
            "`monoallelic_and_biallelic`: BOTH monoallelic and biallelic, autosomal or pseudoautosomal <br>" +
            "`monoallelic_and_more_severe_biallelic`: BOTH monoallelic and biallelic, autosomal or " +
            "pseudoautosomal (but BIALLELIC mutations cause a more SEVERE disease form), autosomal or " +
            "pseudoautosomal <br>" +
            "`xlinked_biallelic`: X-LINKED: hemizygous mutation in males, biallelic mutations in females<br>" +
            "`xlinked_monoallelic`: X linked: hemizygous mutation in males, monoallelic mutations in females " +
            "may cause disease (may be less severe, later onset than males) <br>" +
            "`mitochondrial`: MITOCHONDRIAL <br>" +
            "`unknown`: Unknown <br>" +
            "`NA`: Not applicable";
    private static final String TRANSCRIPT_API_PARAM =  "The Ensembl transcript identifier for the specific " +
            "transcript affected by the variant if any. The transcript must be overlapped by the variant";

    @POST
    @Path("/insert")
    @ApiOperation(value = "Insert a known variant", position = 1,
            notes = "Inserts a known variant. <br>"
                    + "Required values: [submitter, chromosome, position, reference, alternate] <br>"
                    + "Chromosome name normalization, left alignment and trimming is applied before storing the variant."
                    + "<br> Multi-allelic variants are not supported. <br>",
            response = KnownVariantWrapper.class)
    public Response insertKnownVariant(
            @ApiParam(value = SUBMITTER_API_PARAM, required = true)
            @QueryParam("submitter") String submitter,
            @ApiParam(value = CHROMOSOME_API_PARAM,
                    required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = POSITION_API_PARAM, required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = REFERENCE_API_PARAM, required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = ALTERNATE_API_PARAM, required = true)
            @QueryParam("alternate") String alternate) {

        try {
            List<KnownVariantWrapper> result = knownVariantManager.createKnownVariant(
                    submitter,
                    chromosome,
                    position,
                    reference,
                    alternate
            );
            List<KnownVariant> results = new LinkedList<>();
            for (KnownVariantWrapper knownVariantWrapper : result) {
                results.add(knownVariantWrapper.getImpl());
            }
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
                    "Chromosome name normalization, left alignment and trimming is applied before searching for the " +
                    "variant. <br>",
            response = KnownVariantWrapper.class)
    public Response addCuration(
            @ApiParam(value = "The variant's curator (should be gone once user management works)", required = true)
            @QueryParam("curator") String curator,
            @ApiParam(value = CHROMOSOME_API_PARAM,
                    required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = POSITION_API_PARAM, required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = REFERENCE_API_PARAM, required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = ALTERNATE_API_PARAM, required = true)
            @QueryParam("alternate") String alternate,
            @ApiParam(value = PHENOTYPE_API_PARAM,
                    required = true)
            @QueryParam("phenotype") String phenotype,
            @ApiParam(value = INHERITANCE_API_PARAM)
            @QueryParam("modeOfInheritance") ReportedModeOfInheritance modeOfInheritance,
            @ApiParam(value = TRANSCRIPT_API_PARAM)
            @QueryParam("transcript") String transcript,
            @ApiParam(value = "Mendelian variants classification with ACMG terminology as defined in Richards, " +
                    "S. et al. (2015). Standards and guidelines for the interpretation of sequence variants: a joint " +
                    "consensus recommendation of the American College of Medical Genetics and Genomics and the " +
                    "Association for Molecular Pathology. Genetics in Medicine, 17(5),405–423. " +
                    "https://doi.org/10.1038/gim.2015.30.<br>" +
                    "Classification for pharmacogenomic variants, variants associated to disease and somatic " +
                    "variants based on the ACMG recommendations and ClinVar classification " +
                    "(https://www.ncbi.nlm.nih.gov/clinvar/docs/clinsig/).<br>" +
                    "<br>" +
                    "`benign_variant` : Benign variants interpreted for Mendelian disorders<br>" +
                    "`likely_benign_variant` : Likely benign variants interpreted for Mendelian disorders with a " +
                    "certainty of at least 90%<br>" +
                    "`pathogenic_variant` : Pathogenic variants interpreted for Mendelian disorders<br>" +
                    "`likely_pathogenic_variant` : Likely pathogenic variants interpreted for Mendelian disorders " +
                    "with a certainty of at least 90%<br>" +
                    "`uncertain_significance` : Uncertain significance variants interpreted for Mendelian " +
                    "disorders. Variants with conflicting evidences should be classified as uncertain_significance<br>" +
                    "`drug_response` : Drug response variant for pharmacogenomic evidence<br>" +
                    "`established_risk_allele` : Established risk allele for variants associated to disease<br>" +
                    "`likely_risk_allele` : Likely risk allele for variants associated to disease<br>" +
                    "`uncertain_risk_allele` : Uncertain risk allele for variants associated to disease<br>" +
                    "`responsive` : Responsive variants for somatic variants<br>" +
                    "`resistant` : Resistant variants for somatic variants<br>" +
                    "`driver` : Driver variants for somatic variants<br>" +
                    "`passenger` : Passenger variants for somatic variants", required = true)
            @QueryParam("curationClassification") CurationClassification curationClassification,
            @ApiParam(value = "Manual curation confidence")
            @QueryParam("manualCurationConfidence") ManualCurationConfidence manualCurationConfidence,
            @ApiParam(value = "The consistency of evidences for a given phenotype based on the pathogenicity or " +
                    "benignity of evidences. This aggregates all evidences " +
                    "for a given phenotype and all evidences with no phenotype associated (e.g.: in silico impact " +
                    "prediction, population frequency).<br>" +
                    "If not provided this value will be set automatically based on the existing evidences. <br>" +
                    "<br>" +
                    "`consensus` : All evidences are consistent.<br>" +
                    "`conflict` : There are conflicting evidences. This should correspond to a " +
                    "`CurationClassification` of `uncertain_significance` for mendelian disorders.<br>" +
                    "`resolved_conflict` : There are conflicting evidences that have been reviewed and resolved " +
                    "by a curator.")
            @QueryParam("consistencyStatus") ConsistencyStatus consistencyStatus,
            @ApiParam(value = "The penetrance of the phenotype for this genotype. Value in the range [0, 1]")
            @QueryParam("penetrance") Float penetrance,
            @ApiParam(value = "Variable expressivity of a given phenotype for the same genotype")
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
                    "Chromosome name normalization, left alignment and trimming is applied before searching for " +
                    "the variant. <br>",
            response = KnownVariantWrapper.class)
    public Response addEvidence(
            @ApiParam(value = SUBMITTER_API_PARAM, required = true)
            @QueryParam("submitter") String submitter,
            @ApiParam(value = CHROMOSOME_API_PARAM,
                    required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = POSITION_API_PARAM, required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = REFERENCE_API_PARAM, required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = ALTERNATE_API_PARAM, required = true)
            @QueryParam("alternate") String alternate,
            @ApiParam(value = "Name of source")
            @QueryParam("sourceName") String sourceName,
            @ApiParam(value = "The source type of an evidence:<br>" +
                    "<br>" +
                    "`database` : Database (e.g.: ClinVar)<br>" +
                    "`clinical_testing` : Clinical testing (e.g.: any clinical testing external to 100K " +
                    "Genomes Project)<br>" +
                    "`research` : Research study (e.g.: CFTR2)<br>" +
                    "`trial` : Trial results<br>" +
                    "`literatura_manual_curation` : An article from the literature manually curated<br>" +
                    "`literature_automatic_curation` : An article from the literature automatically curated<br>" +
                    "`literature_database_curation` : An article from the literature referenced in a database " +
                    "(e.g.: ClinVar references to PubMed)<br>" +
                    "`insilico_impact_prediction` : Aggregation of in silico predictions. Independent in " +
                    "silico predictions should not be added as evidence<br>" +
                    "`population_allele_frequency` : Allele frequency in the population (e.g.: ExAC)<br>" +
                    "`conservation` : Evolutionary conservation<br>" +
                    "`other` : Any other type", required = true)
            @QueryParam("sourceType") SourceType sourceType,
            @ApiParam(value = "Version of source")
            @QueryParam("sourceVersion") String sourceVersion,
            @ApiParam(value = "URL of source")
            @QueryParam("sourceUrl") String sourceUrl,
            @ApiParam(value = "ID of record in the source")
            @QueryParam("sourceId") String sourceId,
            @ApiParam(value = "Allele origin")
            @QueryParam("alleleOrigin") AlleleOrigin alleleOrigin,
            @ApiParam(value = PHENOTYPE_API_PARAM)
            @QueryParam("phenotype") String phenotype,
            @ApiParam(value = INHERITANCE_API_PARAM)
            @QueryParam("modeOfInheritance") ReportedModeOfInheritance modeOfInheritance,
            @ApiParam(value = TRANSCRIPT_API_PARAM)
            @QueryParam("transcript") String transcript,
            @ApiParam(value = "Evidence of pathogenicity as defined in Richards, S. et al. (2015). Standards " +
                    "and guidelines for the interpretation of sequence variants: a joint consensus recommendation " +
                    "of the American College of Medical Genetics and Genomics and the Association for Molecular " +
                    "Pathology. Genetics in Medicine, 17(5), 405–423. https://doi.org/10.1038/gim.2015.30<br>" +
                    "<br>" +
                    "`very_strong`:<br>" +
                    "    - PVS1 null variant (nonsense, frameshift, canonical ±1 or 2 splice sites, initiation " +
                    "codon, single or multiexon deletion) in a gene where LOF is a known mechanism of disease<br>" +
                    "`strong`:<br>" +
                    "    - PS1 Same amino acid change as a previously established pathogenic variant regardless of " +
                    "nucleotide change<br>" +
                    "    - PS2 De novo (both maternity and paternity confirmed) in a patient with the disease and " +
                    "no family history<br>" +
                    "    - PS3 Well-established in vitro or in vivo functional studies supportive of a damaging " +
                    "effect on the gene or gene product<br>" +
                    "    - PS4 The prevalence of the variant in affected individuals is significantly increased " +
                    "compared with the prevalence in controls<br>" +
                    "`moderate`:<br>" +
                    "    - PM1 Located in a mutational hot spot and/or critical and well-established functional " +
                    "domain (e.g., active site of an enzyme) without benign variation<br>" +
                    "    - PM2 Absent from controls (or at extremely low frequency if recessive) in Exome " +
                    "Sequencing Project, 1000 Genomes Project, or Exome Aggregation Consortium<br>" +
                    "    - PM3 For recessive disorders, detected in trans with a pathogenic variant<br>" +
                    "    - PM4 Protein length changes as a result of in-frame deletions/insertions in a " +
                    "nonrepeat region or stop-loss variants<br>" +
                    "    - PM5 Novel missense change at an amino acid residue where a different missense change " +
                    "determined to be pathogenic has been seen before<br>" +
                    "    - PM6 Assumed de novo, but without confirmation of paternity and maternity<br>" +
                    "`supporting`:<br>" +
                    "    - PP1 Cosegregation with disease in multiple affected family members in a gene " +
                    "definitively known to cause the disease<br>" +
                    "    - PP2 Missense variant in a gene that has a low rate of benign missense variation and " +
                    "in which missense variants are a common mechanism of disease<br>" +
                    "    - PP3 Multiple lines of computational evidence support a deleterious effect on the gene " +
                    "or gene product (conservation, evolutionary, splicing impact, etc.)<br>" +
                    "    - PP4 Patient’s phenotype or family history is highly specific for a disease with a " +
                    "single genetic etiology<br>" +
                    "    - PP5 Reputable source recently reports variant as pathogenic, but the evidence is " +
                    "not available to the laboratory to perform an independent evaluation <br>" +
                    "<br>" +
                    "<b>NOTE: either evidence of pathogenicity or evidence of benignity must be provided, but not both " +
                    "</b>")
            @QueryParam("evidencePathogenicity") EvidencePathogenicity evidencePathogenicity,
            @ApiParam(value = "Evidence of benignity as defined in Richards, S. et al. (2015). Standards and " +
                    "guidelines for the interpretation of sequence variants: a joint consensus recommendation " +
                    "of the American College of Medical Genetics and Genomics and the Association for " +
                    "Molecular Pathology. Genetics in Medicine, 17(5), 405–423. https://doi.org/10.1038/gim.2015.30<br>" +
                    "<br>" +
                    "`stand_alone`:<br>" +
                    "    - BA1 Allele frequency is >5% in Exome Sequencing Project, 1000 Genomes Project, or " +
                    "    Exome Aggregation Consortium<br>" +
                    "`strong`:<br>" +
                    "    - BS1 Allele frequency is greater than expected for disorder<br>" +
                    "    - BS2 Observed in a healthy adult individual for a recessive (homozygous), dominant " +
                    "   (heterozygous), or X-linked (hemizygous) disorder, with full penetrance expected at an " +
                    "   early age<br>" +
                    "    - BS3 Well-established in vitro or in vivo functional studies show no damaging effect on " +
                    "   protein function or splicing<br>" +
                    "    - BS4 Lack of segregation in affected members of a family<br>" +
                    "`supporting`:<br>" +
                    "    - BP1 Missense variant in a gene for which primarily truncating variants are known to " +
                    "   cause disease<br>" +
                    "    - BP2 Observed in trans with a pathogenic variant for a fully penetrant dominant " +
                    "   gene/disorder or observed in cis with a pathogenic variant in any inheritance pattern<br>" +
                    "    - BP3 In-frame deletions/insertions in a repetitive region without a known function<br>" +
                    "    - BP4 Multiple lines of computational evidence suggest no impact on gene or gene " +
                    "   product (conservation, evolutionary, splicing impact, etc.)<br>" +
                    "    - BP5 Variant found in a case with an alternate molecular basis for disease<br>" +
                    "    - BP6 Reputable source recently reports variant as benign, but the evidence is not " +
                    "   available to the laboratory to perform an independent evaluation<br>" +
                    "    - BP7 A synonymous (silent) variant for which splicing prediction algorithms predict no " +
                    "   impact to the splice consensus sequence nor the creation of a new splice site AND the " +
                    "   nucleotide is not highly conserved <br>" +
                    "<br>" +
                    "<b>NOTE: either evidence of pathogenicity or evidence of benignity must be provided, but not both " +
                    "</b>")
            @QueryParam("evidenceBenignity") EvidenceBenignity evidenceBenignity,
            @ApiParam(value = "Identifier in PubMed for literature evidences")
            @QueryParam("pubmedId") String pubmedId,
            @ApiParam(value = "Study name")
            @QueryParam("study") String study,
            @ApiParam(value = "Number of studied individuals")
            @QueryParam("numberOfIndividuals") Integer numberOfIndividuals,
            @ApiParam(value = "This is the list of ethnics in ONS16<br>" +
                    "<br>" +
                    "`D`:  Mixed: White and Black Caribbean<br>" +
                    "`E`:  Mixed: White and Black African<br>" +
                    "`F`:  Mixed: White and Asian<br>" +
                    "`G`:  Mixed: Any other mixed background<br>" +
                    "`A`:  White: British<br>" +
                    "`B`:  White: Irish<br>" +
                    "`C`:  White: Any other White background<br>" +
                    "`L`:  Asian or Asian British: Any other Asian background<br>" +
                    "`M`:  Black or Black British: Caribbean<br>" +
                    "`N`:  Black or Black British: African<br>" +
                    "`H`:  Asian or Asian British: Indian<br>" +
                    "`J`:  Asian or Asian British: Pakistani<br>" +
                    "`K`:  Asian or Asian British: Bangladeshi<br>" +
                    "`P`:  Black or Black British: Any other Black background<br>" +
                    "`S`:  Other Ethnic Groups: Any other ethnic group<br>" +
                    "`R`:  Other Ethnic Groups: Chinese<br>" +
                    "`Z`:  Not stated")
            @QueryParam("ethnicCategory") EthnicCategory ethnicCategory,
            @ApiParam(value = "Description")
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

            @ApiParam(value = CHROMOSOME_API_PARAM, required = true)
            @QueryParam("chromosome") String chromosome,
            @ApiParam(value = POSITION_API_PARAM, required = true)
            @QueryParam("position") Integer position,
            @ApiParam(value = REFERENCE_API_PARAM, required = true)
            @QueryParam("reference") String reference,
            @ApiParam(value = ALTERNATE_API_PARAM, required = true)
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