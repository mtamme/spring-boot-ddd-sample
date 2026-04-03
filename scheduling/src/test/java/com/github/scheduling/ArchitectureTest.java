package com.github.scheduling;

import com.github.seedwork.core.problem.ProblemException;
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
import org.springframework.stereotype.Service;

@AnalyzeClasses(
  packages = ArchitectureTest.ROOT_PACKAGE,
  importOptions = {
    ImportOption.DoNotIncludeJars.class,
    ImportOption.DoNotIncludeTests.class})
class ArchitectureTest {

  static final String ROOT_PACKAGE = "com.github.scheduling";

  private static final String DOMAIN_PACKAGE = ROOT_PACKAGE + ".domain..";
  private static final String APPLICATION_PACKAGE = ROOT_PACKAGE + ".application..";
  private static final String INFRASTRUCTURE_PACKAGE = ROOT_PACKAGE + ".infrastructure..";

  private static final String DOMAIN_LAYER = "domain";
  private static final String APPLICATION_LAYER = "application";
  private static final String INFRASTRUCTURE_LAYER = "infrastructure";

  @ArchTest
  void layerDependenciesMustFollowDomainCentricArchitecture(final JavaClasses classes) {
    Architectures.layeredArchitecture()
      .consideringOnlyDependenciesInLayers()
      .layer(DOMAIN_LAYER).definedBy(DOMAIN_PACKAGE)
      .layer(APPLICATION_LAYER).definedBy(APPLICATION_PACKAGE)
      .layer(INFRASTRUCTURE_LAYER).definedBy(INFRASTRUCTURE_PACKAGE)
      .whereLayer(DOMAIN_LAYER).mayNotAccessAnyLayer()
      .whereLayer(APPLICATION_LAYER).mayOnlyAccessLayers(DOMAIN_LAYER)
      .whereLayer(INFRASTRUCTURE_LAYER).mayOnlyAccessLayers(DOMAIN_LAYER, APPLICATION_LAYER)
      .because("layer dependencies must point inward to keep the domain stable and independent of delivery and technical concerns")
      .check(classes);
  }

  @ArchTest
  void domainLayerMustOnlyDependOnApprovedPackages(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        "java..",
        "org.springframework.stereotype..",
        "com.github.seedwork.core..",
        "com.github.seedwork.domain..",
        DOMAIN_PACKAGE)
      .because("the domain layer must stay technology-agnostic and must depend only on explicitly approved packages to prevent infrastructure concerns from leaking into it")
      .check(classes);
  }

  @ArchTest
  void domainLayerMustOnlyContainDomainConcepts(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideInAPackage(DOMAIN_PACKAGE)
      .and().areNotAnonymousClasses()
      .should().beAssignableTo(Entity.class)
      .orShould().beAssignableTo(Event.class)
      .orShould().beAssignableTo(ValueObject.class)
      .orShould().beAnnotatedWith(Service.class)
      .orShould().beAssignableTo(ProblemException.class)
      .orShould().beInterfaces()
      .because("the domain layer must only contain domain concepts")
      .check(classes);
  }

  @ArchTest
  void applicationLayerMustOnlyDependOnApprovedPackages(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .classes()
      .that().resideInAPackage(APPLICATION_PACKAGE)
      .should().onlyDependOnClassesThat().resideInAnyPackage(
        "java..",
        "org.springframework.context.event..",
        "org.springframework.security..",
        "org.springframework.stereotype..",
        "org.springframework.transaction..",
        DOMAIN_PACKAGE,
        APPLICATION_PACKAGE)
      .because("the application layer implements use cases by orchestrating the domain model and must depend only on explicitly approved packages to prevent infrastructure concerns from leaking into it")
      .check(classes);
  }

  @ArchTest
  void entitiesMustNotExposeBeanGetters(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .methods()
      .that().areDeclaredInClassesThat().areAssignableTo(Entity.class)
      .should().haveNameNotMatching("^get[A-Z].*")
      .because("entities are domain concepts and must not expose state via JavaBean-style getters")
      .check(classes);
  }

  @ArchTest
  void entitiesMustNotExposeBeanSetters(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .methods()
      .that().areDeclaredInClassesThat().areAssignableTo(Entity.class)
      .should().haveNameNotMatching("^set[A-Z].*")
      .because("entities are domain concepts and must not expose JavaBean-style setters that allow unrestricted mutation")
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

  @ArchTest
  void eventsMustBeImmutable(final JavaClasses classes) {
    ArchRuleDefinition.priority(Priority.HIGH)
      .fields()
      .that().areDeclaredInClassesThat().areAssignableTo(Event.class)
      .should().beFinal()
      .because("events are domain concepts and must be immutable")
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
}
