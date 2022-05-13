# ZTXChain Java SDK

## Introduction
Java developers can easily operate Gas blockchain via the ZTXChain Java SDK. And you can complete the installation of the SDK in Maven or by downloading the jar package in a few minutes.

1. [docs](https://docs.zetrix.com/en/sdk/java) are the usage documentations for the ZTXChain Java SDK.
2. [examples](/examples) are some examples of a project based on Maven.
2. [src](/src)  is the source code for the ZTXChain Java SDK.

## Environment

JDK 8 or above.

## Installation

> Note: V1.x has been prompted for maintenance, and V2.x is recommended.

#### Mode 1：Adding Dependencies to Maven Projects (Recommended)
To use the ZTXChain Java SDK in a Maven project, just add the remote repository provided by Gas to the maven configuration and add the corresponding dependency to pom.xml.

This article uses version 1.0.5 as an example

Add the following in the dependencies tag：
``` xml
<dependency>
  <groupId>org.zetrix.sdk</groupId>
  <artifactId>zetrix-sdk</artifactId>
  <version>1.0.5</version>
</dependency>
```
#### Mode 2: Import the JAR Package in the Project
1. Download ZTXChain Java SDK Development Kit
2. Unzip the development package
3. Import zetrix-sdk-{version}.jar and the included libs jar into your project

## Example project
ZTXChain Java SDK provides rich examples for developers' reference
