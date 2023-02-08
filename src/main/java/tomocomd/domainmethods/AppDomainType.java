package tomocomd.domainmethods;

import ambit2.model.numeric.DataCoverage;
import ambit2.model.numeric.DataCoverageDensity;
import ambit2.model.numeric.DataCoverageDescriptors;
import ambit2.model.numeric.distance.DataCoverageDistanceCityBlock;
import ambit2.model.numeric.distance.DataCoverageDistanceEuclidean;
import ambit2.model.numeric.distance.DataCoverageDistanceMahalanobis;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum AppDomainType {
    MODE_RANGE("_modeRANGE", new DataCoverageDescriptors()),
    MODE_EUCLIDEAN("_modeEUCLIDEAN",new DataCoverageDistanceEuclidean()),
    MODE_CITY_BLOCK("_modeCITYBLOCK", new DataCoverageDistanceCityBlock()),
    MODE_MAHALANOBIS("_modeMAHALANOBIS", new DataCoverageDistanceMahalanobis()),
    MODE_DENSITY("_modeDENSITY",new DataCoverageDensity());

    private final String name;
    public  final DataCoverage dataCoverage;

}
