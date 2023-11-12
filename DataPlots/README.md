# The ModelQueryTool plotting project
This is a side project, made fully in Python, that provides some tools for plotting as well as remeshing.
Besides the python scripts there are three folders:
1. `figures`, which is the output folder for many of the python scripts
2. `shared`, this is a communication folder between the Java and the Python code. You should never have to touch the files in this directory: They are all fully generated from code.
3. `tools` contains a set of scripts used by the Java code.

## Libraries
To run all the scripts in this project, the following libraries are needed:
1. matplotlib
2. pandas
3. numpy
4. sklearn

All of these can be installed via the `pip` command.
