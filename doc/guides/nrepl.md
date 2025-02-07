# nREPL

If you would like to send every REPL eval to Portal, you can use the
`portal.nrepl/wrap-portal` nrepl middleware.

> **Note** You will also need to invoke `(add-tap portal.api/submit)` for eval
> results to show up in portal.

The main advantage of using this middleware is that it can capture additional
information about your REPL interaction, such as:

- Source and file info
- Runtime type (very handy for cljc files)
- Timings
- Test assertion output
- Eval exceptions
- Stdio

![Screen Shot 2022-10-15 at 2 34 12 PM](https://user-images.githubusercontent.com/1986211/196008409-4804c548-6203-4c53-93ab-625c0104d1c8.png)

> **Note** Portal will keep all evaluated objects from being garbage collected
> until they are cleared from the UI. However, the nREPL middleware will only
> submit values to Portal when the UI is open.

## tools.deps

If you are starting nrepl from tools.deps, you can try the following:

```clojure
;; deps.edn
{:aliases
 {:nrepl
  {:extra-deps {cider/cider-nrepl {:mvn/version "0.28.5"}}
   :main-opts ["-m" "nrepl.cmdline"
               "--middleware"
               "[cider.nrepl/cider-middleware,portal.nrepl/wrap-portal]"]}}}
```

## shadow-cljs

If you are using shadow-cljs, you can add the middleware via the
`shadow-cljs.edn` file:

```clojure
;; shadow-cljs.edn
{:nrepl {:middleware [portal.nrepl/wrap-portal]}}
```
