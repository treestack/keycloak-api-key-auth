# Keycloak API Key authenticator

This Authentication SPI allows the "misuse" of the Resource Owner Password Credentials grant to identify an user by 
a pre-generated API key stored as a user attribute.

Before using this SPI, please keep in mind that exposing user credentials like API keys to the outside world
significantly increases the attack surface of your application.

## Resource Owner Password Credentials

OAuth's Resource Owner Password Credentials (ROPC) grant type, also known as the "password grant," was designed as a 
means of migrating legacy authentication mechanisms to an OAuth tokenized architecture. It essentially swaps user 
credentials for tokens without properly obtaining the user's consent or conducting proper identification. 

It is only suitable in low security cases where the resource owner has a trust relationship with the client, so please 
be aware of the security implications.

## Requirements

This module was developed for Keycloak 22. For earlier versions please see https://github.com/carbonrider/keycloak-api-key-module.

## Build and deploy

Execute `mvn package` in the root directory. The deployable JAR file will be created in the `target/`-folder.

Copy the jar file into Keycloaks `providers` directory. If you use the official Docker container you can mount a volume 
at `/opt/keycloak/providers`. 

## Configuration

Create a client and enable 'Direct access grants'. Please take a moment to consider the ramifications.

![](doc/capability_config.png)

Create a new authentication flow:

![](doc/create_flow.png)

Choose a name (e.g. "direct grant with api key") and add a single execution:

![](doc/add_step.png)

Set the requirement to "Required" and you're done. You can optionally configure the name of the custom user attribute
that contains the API key. It defaults to `api-key'.

Go back to the authentication flows list and bind your new flow to the direct grant flow.

![](doc/bind_flow.png)

Choose binding type "Direct grant flow" and we're done here.

## Create API key

The API key must be stored as an user attribute. Please create a user and add an attribute with the name you chose in
the previous step.

![](doc/user_attribute.png)

## Usage

You can now exchange the API key for tokens by sending a POST request to the Token endpoint of your realm, and Keycloak 
will respond with the generated tokens.

```
curl --location 'http://keycloak:8080/realms/your-realm/protocol/openid-connect/token' \
  --header 'Content-Type: application/x-www-form-urlencoded' \
  --data-urlencode 'grant_type=password' \
  --data-urlencode 'client_id=client-name' \
  --data-urlencode 'api_key=1230f76a-4325-4563-9af6-bea7f4d5b622'
```