#! /bin/bash -xe

pandoc --read=markdown_github \
    --filter ./tikz.py \
    -s README.md \
    -V title="\textbf{Lock-free Skip List}" \
    -V author="Alec Bargas \\\\ Julian Domingo \\\\ Jacob Ingalls \\\\ John Starich \\\\ EE 360P Concurrent and Distributed Programming \\\\ Class: T TH 3:30 - 5:00 PM" \
    -V margin-left=1in \
    -V margin-right=1in \
    -V margin-top=1in \
    -V margin-down=1in \
    -f "markdown+tex_math_single_backslash+escaped_line_breaks" \
    -o report.pdf

sleep 1
open report.pdf
