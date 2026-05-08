# Integrations

Http4s provides a standard interface for defining services and clients. This enables an
ecosystem of interchangeable server and client backends. 

## Ember

Http4s Ember is a server and client backend developed in the core repository. It implements
HTTP/1 and HTTP/2. Runs on JDK 8+, Node.js 16+ and Scala Native. Pure FP, built with Cats 
Effect and FS2

```scala
libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-ember-server" % "0.23.34",
  "org.http4s" %% "http4s-ember-client" % "0.23.34",
)
```

## Backend Integrations

Backend 	        Platform 	                    Http Client 	Http Server 	Websocket Client 	Websocket Server 	Proxy support (Client)
Ember 	            JDK 8+ / Node.js 16+ / Native 	    ✅ 	            ✅ 	            ❌              	✅ 	                    ❌
Blaze 	            JDK 8+ 	                            ✅ 	            ✅ 	            ❌ 	                ✅                  	❌
Netty 	            JDK 8+                          	✅ 	            ✅ 	            ✅ 	                ✅ 	                    ✅
JDK Http Client 	JDK 11+ 	                        ✅ 	            ❌ 	            ✅              	❌                      ✅
Servlet 	        JDK 8+ 	                            ❌ 	            ✅ 	            ❌ 	                ❌ 	                    ❌
DOM 	            Browsers 	                        ✅          	❌ 	            ✅ 	                ❌                  	❌
Feral 	            Serverless 	                        ❌ 	            ✅ 	            ❌ 	                ❌ 	                    ❌

## Entity Integrations

Http4s has multiple smaller modules for Entity encoding and decoding support of common types.
    - [https://http4s.org/v0.23/docs/json.html](Circe)
