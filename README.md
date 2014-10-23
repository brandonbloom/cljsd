# cljsd

This project doesn't do anything yet, but it may someday be a basis for simpler
and less painful ClojureScript tooling.

Born of my frustration with `cljsbuild`, especially `lein cljsbuild auto`.


# Design

## Assume JVM Server + Browser

I'm not interested running on Node or other non-web-app use cases. A browser
REPL should always be available.

## Build on Demand During Development

When I refresh my browser, I want to be confident that I'm getting the latest
output. I don't want to wait before pressing refresh. I don't want to wait for
one compile per file changed. The ClojureScript compiler should run in response
to web requests, rather than filesystem changes.

REPL based development with incremental evaluation of forms and file should be
the preferred way of working. Refreshing the browser should be only slightly
less rare than restarting my Clojure/JVM REPL.

## Save != Evaluate

A great deal of power and flexibility comes from separating "save to disk" from
"evaluate in current environment". Auto-building with cljsbuild conflates the
two, and auto-loading with [Figwheel][1] exacerbates the problem.

## Minimal Configuration

Only three values are required:

- Directory path of ClojureScript source
- Directory path for JavaScript output
- Subpath of URL to mount the JavaScript output

Original cljs, source maps, unoptimized js, and optimized js are all
world-visible. Optimization is requested by requesting `.min.js` files.

## Library, Not Framework

A very small API will be provided for the following tasks:

- Mount the app via Ring middleware
- Precompile everything to the output directory
- Connect to a browser REPL

No Lein integration will be provided.

## Servable Output Directory

The cljsd middlewhere should only be required during development. In production
builds, it should suffice to simply publish the entire output directory with a
standard web server.


## License

Copyright © 2014 Brandon Bloom

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.


[1]: https://github.com/bhauman/lein-figwheel
