package com.github.seedwork;

import com.github.seedwork.domain.Entity;
import com.github.seedwork.domain.Event;
import com.github.seedwork.domain.ValueObject;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.Priority;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.library.Architectures;

@AnalyzeClasses(
  packages = ArchitectureTest.ROOT_PACKAGE,
  importOptions = {
    ImportOption.DoNotIncludeJars.class,
    ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  static final String ROOT_PACKAGE = "com.github.seedwork";

  private static final String CORE_PACKAGE = ROOT_PACKAGE + ".core..";
  private static final String DOMAIN_PACKAGE = ROOT_PACKAGE + ".domain..";
  private static final String INFRASTRUCTURE_PACKAGE = ROOT_PACKAGE + ".infrastructure..";

  private static final String CORE_LAYER = "core";
  private static final String DOMAIN_LAYER = "domain";
  private static final String INFRASTRUCTURE_LAYER = "infrastructure";

  @ArchTest
  void layerDependenciesMustFollowDomainCentricArchitecture(final JavaClasses classes) {
    Architectures.layeredArchitecture()
      .consideringOnlyDependenciesInLayers()
      .ensureAllClassesAreContainedInArchitectureIgnoring(ROOT_PACKAGE)
      .optionalLayer(CORE_LAYER).definedBy(CORE_PACKAGE)
      .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
      .layer(INFRASTRUCTURE_LAYER).definedBy(INFRASTRUCTURE_PACKAGE)
      .whereLayer(CORE_LAYER).mayNotAccessAnyLayer()
      .whereLayer(DOMAIN_LAYER).mayOnlyAccessLayers(CORE_LAYER)
      .whereLayer(INFRASTRUCTURE_LAYER).mayOnlyAccessLayers(CORE_LAYER, DOMAIN_LAYER)
      .because("layer dependencies must point inward to keep the domain stable and independent of delivery and technical concerns")
      .check(classes);
  }

  @ArchTest
  void coreLayerMustOnlyDependOnApprovedPackages(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideInAnyPackage(CORE_PACKAGE)
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        "java..",
        CORE_PACKAGE)
      .because("the core layer must stay technology-agnostic and must depend only on explicitly approved packages to prevent infrastructure concerns from leaking into it")
      .check(classes);
  }

  @ArchTest
  void domainLayerMustOnlyDependOnApprovedPackages(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideInAnyPackage(DOMAIN_PACKAGE)
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        "java..",
        CORE_PACKAGE,
        DOMAIN_PACKAGE)
      .because("the domain layer must stay technology-agnostic and must depend only on explicitly approved packages to prevent infrastructure concerns from leaking into it")
      .check(classes);
  }

  @ArchTest
  void entitiesMustResideInDomainLayer(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideOutsideOfPackage(DOMAIN_PACKAGE)
      .should().notBeAssignableTo(Entity.class)
      .because("entities are domain concepts and must not be defined outside the domain layer")
      .check(classes);
  }

  @ArchTest
  void valueObjectsMustResideInDomainLayer(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideOutsideOfPackage(DOMAIN_PACKAGE)
      .should().notBeAssignableTo(ValueObject.class)
      .because("value objects are domain concepts and must not be defined outside the domain layer")
      .check(classes);
  }

  @ArchTest
  void eventsMustResideInDomainLayer(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideOutsideOfPackage(DOMAIN_PACKAGE)
      .should().notBeAssignableTo(Event.class)
      .because("events are domain concepts and must not be defined outside the domain layer")
      .check(classes);
  }
}
