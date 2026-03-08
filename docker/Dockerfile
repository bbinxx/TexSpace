FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive

RUN apt-get update && apt-get install -y --no-install-recommends \
    texlive-full \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# Run as non-root user for extra security
RUN useradd -m texuser
USER texuser

ENTRYPOINT ["pdflatex", "-interaction=nonstopmode", "-halt-on-error"]
CMD ["main.tex"]
