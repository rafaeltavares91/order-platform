package dev.study.orderplatform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.study.orderplatform.domain")
class DomainArchitectureTest {

    @ArchTest
    static final ArchRule domain_is_framework_and_infrastructure_free = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "..configuration..",
                    "..messaging..",
                    "..persistence..",
                    "..web..");
}
