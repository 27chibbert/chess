# ♕ BYU CS 240 Chess

This project demonstrates mastery of proper software design, client/server architecture, networking using HTTP and WebSocket, database persistence, unit testing, serialization, and security.

## 10k Architecture Overview

The application implements a multiplayer chess server and a command line chess client.

[![Sequence Diagram](10k-architecture.png)](https://sequencediagram.org/index.html#initialData=C4S2BsFMAIGEAtIGckCh0AcCGAnUBjEbAO2DnBElIEZVs8RCSzYKrgAmO3AorU6AGVIOAG4jUAEyzAsAIyxIYAERnzFkdKgrFIuaKlaUa0ALQA+ISPE4AXNABWAexDFoAcywBbTcLEizS1VZBSVbbVc9HGgnADNYiN19QzZSDkCrfztHFzdPH1Q-Gwzg9TDEqJj4iuSjdmoMopF7LywAaxgvJ3FC6wCLaFLQyHCdSriEseSm6NMBurT7AFcMaWAYOSdcSRTjTka+7NaO6C6emZK1YdHI-Qma6N6ss3nU4Gpl1ZkNrZwdhfeByy9hwyBA7mIT2KAyGGhuSWi9wuc0sAI49nyMG6ElQQA)

## Modules

The application has three modules.

- **Client**: The command line program used to play a game of chess over the network.
- **Server**: The command line program that listens for network requests from the client and manages users and games.
- **Shared**: Code that is used by both the client and the server. This includes the rules of chess and tracking the state of a game.

## Starter Code

As you create your chess application you will move through specific phases of development. This starts with implementing the moves of chess and finishes with sending game moves over the network between your client and server. You will start each phase by copying course provided [starter-code](starter-code/) for that phase into the source code of the project. Do not copy a phases' starter code before you are ready to begin work on that phase.

## IntelliJ Support

Open the project directory in IntelliJ in order to develop, run, and debug your code using an IDE.

## Maven Support

You can use the following commands to build, test, package, and run your code.

| Command                    | Description                                     |
| -------------------------- | ----------------------------------------------- |
| `mvn compile`              | Builds the code                                 |
| `mvn package`              | Run the tests and build an Uber jar file        |
| `mvn package -DskipTests`  | Build an Uber jar file                          |
| `mvn install`              | Installs the packages into the local repository |
| `mvn test`                 | Run all the tests                               |
| `mvn -pl shared test`      | Run all the shared tests                        |
| `mvn -pl client exec:java` | Build and run the client `Main`                 |
| `mvn -pl server exec:java` | Build and run the server `Main`                 |

These commands are configured by the `pom.xml` (Project Object Model) files. There is a POM file in the root of the project, and one in each of the modules. The root POM defines any global dependencies and references the module POM files.

## Running the program using Java

Once you have compiled your project into an uber jar, you can execute it with the following command.

```sh
java -jar client/target/client-jar-with-dependencies.jar

♕ 240 Chess Client: chess.ChessPiece@7852e922
```

A sequence for phase 3 implementation is below:
<https://sequencediagram.org/index.html#initialData=IYYwLg9gTgBAwgGwJYFMB2YBQAHYUxIhK4YwDKKUAbpTngUSWDABLBoAmCtu+hx7ZhWqEUdPo0EwAIsDDAAgiBAoAzqswc5wAEbBVKGBx2ZM6MFACeq3ETQBzGAAYAdAE5M9qBACu2AMQALADMABwATG4gMP7I9gAWYDoIPoYASij2SKoWckgQaJiIqKQAtAB85JQ0UABcMADaAAoA8mQAKgC6MAD0PgZQADpoAN4ARP2UaMAAtihjtWMwYwA0y7jqAO7QHAtLq8soM8BICHvLAL6YwjUwFazsXJT145NQ03PnB2MbqttQu0WyzWYyOJzOQLGVzYnG4sHuN1E9SgmWyYEoAAo3h8UGtfv8OGswacAJTXaqiO6VWTyJQqdT1ewoMAAVQGWIGOLJNMUyjUqipRh0tQAYkhODA2ZQeTAdJYYNjZmIdCjgABrSUDGCbJBgeIKzlKmDABCqjjylAADzRGh5dP5dwRFJU9SlUB55JEKkdlRuzxgClNKGA5va6vQAFFLSpsAQCp7bqV7sVzPVAk5gsNxnN1MAmQtlhGoN46gapkaTWb5fI1egoWZOJg7Xz1D6ql6UPU0D4EAgE5SnR36vF9Pr8Tt+977s36apaiBVei3Rzy3M8fo-js2Kp4kTjqSm9p7a2KjP+fUF8H0ZrpdoYBAdAArFDgQ+0lsC+7GWoKDgcG-utob68rOgpngy8CLoYCg+HqMoPs+r7gZ+lTfr+-4wXBQHIW2iIugGsHxB6eGGEmlQwk8paYURQEUXCuHVP6ryGp8MD1PsILAIR7QQLWaBLOxlwJpQbYphg9ThE4ThZhMLHzGxyzAssXF6jxfECYp9boBwpheL4ATQOwTIxCKcARtIcAKDAAAyEBZIUYnMIONT1M0bRdL0BjqPkaBZoqa4wOOAJXH68LkY8cIvP5uKBRuBLQhFInOUiMAIHZ4rLm865bDsZIkVSyH1Cp8SZXJ2WbgC3JHh+grfjAYoSm6MpymW7wVhKQU6SqwYam6RgQGoaAAOTMFaaL3rAnX9YNI0wMcYAgPEwHHp+yX4d2vaTqRzn+gAkmgVAmkgHC2fZUYxnGhShaJyCpjA6YAIwyTmqh5vM7FFiW9TiodyD-peHDmEgJqqFpjY4WRJGugMxHOqR5SFTAaAoJsFDeQUGJNdoVXvqBX7CvV4r-l5qg+bK8rRZg3XqgB2q6vq0XGkGIbyml9hMv+4rLTVZHtlQKXUbDHZtnR-qC7RiXwjtpbMau70cWMxVqXWkIhYxUuVI5ElSTJ0VfCsivcbxKv7Fc2nc7OFR1XAUFIyjMAk2TCEvlgENrZ2BFYfIW0i5L9Ti97osa3zTGyXL+uG6pxv8arwnB1rMCSdJoxh21nyQgbSvR18ZuNp43h+P4XgoOgMRxIkxel6dvhYI5gqha50gRtZEbtBG3Q9I7BTDFnfFx4KQdFUbfdB-XcP1GzNdo6TGO9+geVw2B1WzkPerTz5GJz2gOMgQ6+P1A1xP8mTLVb1TqoauvBR03qxrD6XHADaow2jdaOQW3v7tdj2fb5bzDcwH2r9Y6p1xTnRQLGHy-cyIJ0es9fkb0CxjE+tAb6B0jr-RRIDDAwMECg1zjpN2vpx4ASFvzKc1Jl7niMCgbg6Ir5oE3vfbeH8TyoQJtIWhzJDBdzQPeJ8LtWGrWIUOJGP8fa80HmIzao9-7qxeGMF6iDFhkB8B+AAZj-VmdkOb3lgvWa6vNYE6xTko-MKi1Gzk0b2bR7MUD-hrmDHS+d9L+CwTEbA4oNTWXGgAcSVBoOu0tXK+Nbh3ewSoe7MOgeFWE-oz6yJEeQj2yAcj+JzEwqOfEF7C2nFQiCxUGGZPiMrFhEN2EHyJg7Y+19T7ROppfGpfCdS3y3tNZ+s0xrvyIXzFKG1f6LzkdQPa6C-qgLQOAyB8ZDEVGMU9UxCDzGFmLKgmAP0MEwABkDEGTihEMVEQHYAEjKG42oUyMAgYEDpLUBiHeK1aoEwyBYVANBUrjQgOopmCAYARJzHsoZyT6jXJ5A0ToEj7hSOBdoUFmBZHBJgCMZYvy1AFgaOMZFu1pAFn8CCTY8RdQoDdDiPYqxhjLGSKANURKlQkpBMigAckqRYUIYCdDVsM+Ot1xKJxMYisYyLQauXRUqTF2LcX4qXHJWlZKxgUpAFSqVzK6VKkZXMZlFxWUEN0gXAIHAADsbgnAoCcDECMwQ4BmQAGyQSvIYa5MAihcqckk-0bkOjhMidMZhWYGVKnZYmWJlEXhbzWL6uYCU4lhV6fhS8cgUDXOKaU0NKqlQ5OSUvU5BTCJFK3ncnmFTCYSl4eTO+WT0Dnx6lUdGzT6alpKdHdpL8YBdNdvk4R0aPb9PBS60sQCMHjMmZdaBsynVpicPM7Miz3rLK+ms0Zx1NlYO2Xg3ZPSoakKAn-E5u8ILI02Am6Kya5iqpQHmvGBbD4-KNC1MNyoL4wHtS0-Ut6mZVmbW-MAto237MBdIgZuT4WBirGGPig6oEzM1qO+6GZ4G5iWcglZpZ7WVmDOad9NpV3ftPG2+oNtbV202A+pU8EBFISwwWvDcaCNXrmDffUOJjQSjeP8r+RG5hkIHBCv2NGUCYthZLH9ocMVYtjhB+AUGk4yWEznBszi9KF0sLQtKhHYhIASGARTvYICEYAFIQHFGxww-g5VqkdSUZ1IdSzNBZB5HoyKollt8qMbACBgCKagHACAaUoBHt49If1SVyhSJGCGwKrn3Oee875zFEbKJjyHI+fTaAE2hZc25ygkXoDRekGmgc26Vqr3iDm5hZ694XqqcWupjmK00wYXRutpTG2dI-SxpJfTxF-3hX2sZ6UJnRggUOiDczYOvXgyg0s6y-qLvscu-BsnWsdv9oRDjFCZA4ZgIl8UPED1lTC+ljzXmss8cxaVthQpKkSmRSW4TNWNSPtrcJprr80QLfXV2zrPb-bM1DOGPrF1wPqxuuZsdmYFlwenQh2dyHvsWg-XN7SDrynnaqMwK7eKCU9BM3t9zwxICtRgCiAAjqkd+DTDP1ZfShlm6Gchfsze29dlMPuWa+8B37YHpmA6MVB9MoPJ3g6QeNoFRo0vuYdtgCAYAe4w+NN5ewyNdharXSQ97gyuORu-jIgTAKmIBc5cDnlycRhapcYXLwbmy5qYrlAC3iBgywGANgFzhA8jXzM+YeLLlGhNxbm3DuxgYkPA1wi2L9FWMojSiGBQTvbnHLW-Ti83A8CXNj0juqnC6GGBNN8x3zuQCu74Vob2yvRGq4A4GyKf7+ORsEzLPXQO7qSdGCb-w9LJce2OPKRaEAIAGBgHjkcrywDbCMEgdR6jKAe4GOm524BVBrAKIYdHi0CcoGJ0gFEAp2ANYbY8e8yNtT4pX4-NQ-f4iGDQAUUoxUgb5-RP+InJPP0uHqj4fA5+oAzGgDFPUk+5oW50B4QgTwCoxAFc3UFP3UWgDP0MDeCMG0DWGLSL2ADWD3yu2QIdkgBtyZBcCAA>
