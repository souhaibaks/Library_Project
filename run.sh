#!/bin/bash
cd "$(dirname "$0")"
java --module-path lib --add-modules javafx.controls,javafx.graphics -cp src Main

