package com.github;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.dependencies.SlicesRuleDefinition;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static final String APPLICATION_LAYER_NAME = "application";
  private static final String CORE_LAYER_NAME = "core";
  private static final String DOMAIN_LAYER_NAME = "domain";
  private static final String INFRASTRUCTURE_LAYER_NAME = "infrastructure";

  private static final String BASE_PACKAGE_NAME = ArchitectureTest.class.getPackageName();
  private static final String BOOKING_PACKAGE_NAME = "%s.booking".formatted(BASE_PACKAGE_NAME);
  private static final String SEEDWORK_PACKAGE_NAME = "%s.seedwork".formatted(BASE_PACKAGE_NAME);

  @Test
  void bookingDependenciesShouldRespectLayers() {
    // Arrange
    final var rule = Architectures.layeredArchitecture()
      .consideringOnlyDependenciesInLayers()
      .ensureAllClassesAreContainedInArchitecture()
      .layer(APPLICATION_LAYER_NAME).definedBy("%s.application..".formatted(BOOKING_PACKAGE_NAME))
      .layer(DOMAIN_LAYER_NAME).definedBy("%s.domain..".formatted(BOOKING_PACKAGE_NAME))
      .layer(INFRASTRUCTURE_LAYER_NAME).definedBy("%s.infrastructure..".formatted(BOOKING_PACKAGE_NAME))
      .whereLayer(APPLICATION_LAYER_NAME).mayOnlyAccessLayers(DOMAIN_LAYER_NAME)
      .whereLayer(DOMAIN_LAYER_NAME).mayNotAccessAnyLayer()
      .whereLayer(INFRASTRUCTURE_LAYER_NAME).mayOnlyAccessLayers(APPLICATION_LAYER_NAME, DOMAIN_LAYER_NAME);
    final var classes = new ClassFileImporter().importPackages(BOOKING_PACKAGE_NAME);

    // Act
    // Assert
    rule.check(classes);
  }

  @Test
  void seedworkDependenciesShouldRespectLayers() {
    // Arrange
    final var rule = Architectures.layeredArchitecture()
      .consideringOnlyDependenciesInLayers()
      .ensureAllClassesAreContainedInArchitecture()
      .layer(CORE_LAYER_NAME).definedBy("%s.core..".formatted(SEEDWORK_PACKAGE_NAME))
      .layer(DOMAIN_LAYER_NAME).definedBy("%s.domain..".formatted(SEEDWORK_PACKAGE_NAME))
      .layer(INFRASTRUCTURE_LAYER_NAME).definedBy("%s.infrastructure..".formatted(SEEDWORK_PACKAGE_NAME))
      .whereLayer(CORE_LAYER_NAME).mayNotAccessAnyLayer()
      .whereLayer(DOMAIN_LAYER_NAME).mayOnlyAccessLayers(CORE_LAYER_NAME)
      .whereLayer(INFRASTRUCTURE_LAYER_NAME).mayOnlyAccessLayers(CORE_LAYER_NAME, DOMAIN_LAYER_NAME);
    final var classes = new ClassFileImporter().importPackages(SEEDWORK_PACKAGE_NAME);

    // Act
    // Assert
    rule.check(classes);
  }

  @Test
  void dependenciesShouldRespectSlices() {
    // Arrange
    final var rule = SlicesRuleDefinition.slices()
      .matching("%s.(*)..".formatted(BASE_PACKAGE_NAME))
      .should()
      .notDependOnEachOther()
      .ignoreDependency(DescribedPredicate.alwaysTrue(), JavaClass.Predicates.resideInAPackage("%s..".formatted(SEEDWORK_PACKAGE_NAME)));
    final var classes = new ClassFileImporter().importPackages(BASE_PACKAGE_NAME);

    // Act
    // Assert
    rule.check(classes);
  }
}
