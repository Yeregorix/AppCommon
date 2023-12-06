# AppCommon

AppCommon is a library for developing JavaFX applications that includes many services to improve application quality
easily.

## Java 17+ support

AppCommon applications are compatible with any Java version >= 17. The JavaFX runtime is automatically downloaded when
missing.

## Gradle integration

[AppCommonGradle](https://github.com/Yeregorix/AppCommonGradle) plugin automates JAR packaging. AppCommon is shaded and
dependencies are exported to a JSON file for automatic management.

## Dependency management

The application's dependencies are automatically downloaded and added to the classpath. They don't need to be included
in the main jar so that it stays small.

## Update check

The application can check for updates on GitHub or on a dedicated host. A notification is displayed to the user so that
he can automatically download the update and restart the application.

## Resource manager

A modular resource manager allows the user to select the language and the developer to get the corresponding resources (
strings, images, sounds, etc). Translations can be get as JavaFX string properties that automatically update when the
user changes his language.

## Event manager

A lightweight event manager allows the developer to listen for applications modifications such as its state, the
selected language or any custom event.

## Platform

Basic platform detection is operated so that the developer can easily do OS or architecture specific operations.

## Tasks

A advanced system for listening for progressions of a task. This includes a title, a message and an incremental counter
or a progression. A task might be cancellable or not.

## Popups

A chain-style API is available to easily create popups. This also includes text input or number input dialogs. Built-in
support for tasks and for stack traces.

## Number fields

JavaFX text fields specialized for numbers. This includes automatic parsing, range constraint and characters filter.

## SLF4J logger

The application includes a preconfigured SLF4J implementation for maximum compatibility with other libraries.

## Connection config

The application includes a centralized configuration for managing URL connections.

## Resource loader

Allows processing any JAR resources as a NIO `Path`.

## Utilities

A bunch of utilities classes are provided to facilitate common operations such as recursive file copy,
reflection, `GridPane` rows and columns definition.