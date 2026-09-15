package dev.study.orderplatform.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

@AnalyzeClasses(packages = "dev.study.orderplatform")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domain_is_framework_free = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "..application..",
                    "..configuration..",
                    "..identifier..",
                    "..persistence..",
                    "..web..");

    @ArchTest
    static final ArchRule application_does_not_depend_on_frameworks_or_driven_adapters = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework..",
                    "jakarta.persistence..",
                    "..configuration..",
                    "..identifier..",
                    "..persistence..",
                    "..web..");

    @ArchTest
    static final ArchRule web_does_not_depend_on_infrastructure = noClasses()
            .that().resideInAPackage("..web..")
            .should().dependOnClassesThat().resideInAnyPackage("..identifier..", "..persistence..");
}
