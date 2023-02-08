package tomocomd.domainmethods;

import lombok.AllArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@Setter
public class AppDomainConfig {
    AppDomainType appDomainType;
    String []options;
}
