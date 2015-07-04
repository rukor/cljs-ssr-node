# cljs-ssr-node

A tiny library to support CLJS development development on NodeJS with server side rendering and silk-based route handling.

This initially started out as om-ssr targeting OM the JVM but, seemed a bit to heavy for front-end-only apps that should not have much overhead. Currently, cljs-ssr-node supports both om and reagent based views. It is trivial to add support for other view projects.

## Usage

Add `[com.firstlinq/cljs-ssr-node "0.1.0"]` to your dependencies and `[lein-npm "0.5.0']` to your plugin dependencies. 

Then type `lein npm pprint > package.json`. This will generate a package.json consisting of node dependencies in this project and your project plus that defined in any other dependencies that you have defined. 


This is a pre-alpha quality proof of concept, pull requests welcome.

See [cljs-ssr-hello](http://github.com/rukor/cljs-ssr-hello) for a working sample app, and [cljs-ssr-app](http://github.com/rukor/cljs-ssr-app) for a leiningen template that can be 
used to bootrap webapps based on cljs-ssr-node.

## TODO

- Documentation
- Tests

## License

Copyright © 2015 Roland Ukor

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
