# Corda PromptPay POC

This is a POC project of PromptPay using R3 Corda. Nothing more than that :P

## Summary

This project consist of several modules as follow:
* asset issue and asset transfer using built-in Corda flow (AssetIssueFlow and AssetPaymentFlow)
* proxy name registration (ProxyNameIssueFlow)
* proxy name inquiry using oracle (ProxyNameQueryFlow)
* money transfer using proxy name (AccountTransferProposeFlow and AccountTransferConfirmFlow)

## Pre-Requisites
* [JDK 8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) 
  installed and available on your path (Minimum version: 1.8_131).

if you want to develop, you need additional as follow:
* [IntelliJ IDEA](https://www.jetbrains.com/idea/download/) (Minimum version 2017.1)
* git

## Getting Set Up

To get started, clone this repository with:

     git clone https://github.com/apemon/corda-promptpay-poc.git

And change directories to the newly cloned repo:

     cd corda-promptpay-poc

## Building

**Unix:** 

     ./gradlew deployNodes

**Windows:**

     gradlew.bat deployNodes

## Running the Nodes

Once the build finishes, change directories to the folder where the newly
built nodes are located:

     cd build/nodes

The Gradle build script will have created a folder for each node. You'll
see three folders, one for each node and a `runnodes` script. You can
run the nodes with:

**Unix:**

     ./runnodes

**Windows:**

    runnodes.bat

## Testing

You can interact with Corda via HTTP. You can use postman script that simulate simple scenario by import "promptpay-poc.postman_collection.json"

## Further reading

My medium blog post (Thai version)
[here](https://medium.com/@parin.chiam/%E0%B8%97%E0%B8%94%E0%B8%AA%E0%B8%AD%E0%B8%9A%E0%B8%A5%E0%B8%AD%E0%B8%87%E0%B9%80%E0%B8%82%E0%B8%B5%E0%B8%A2%E0%B8%99-promptpay-%E0%B9%81%E0%B8%9A%E0%B8%9A-dlt-%E0%B8%94%E0%B9%89%E0%B8%A7%E0%B8%A2-r3-corda-bf06abf4020f).
