FROM clojure:lein-alpine
MAINTAINER César Olea <cesarolea@gmail.com>

WORKDIR /app
## CMD ["lein", "fig-dev"]
CMD ["lein", "repl", ":headless", ":host", "0.0.0.0", ":port", "31337"]
