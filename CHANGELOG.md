# Version 1.1.0 -- 2019-01-08

* An expectation can now use a qualified keyword spec to test conformance of the actual value. Failures are reported with the spec explanation. #2
* If Paul Stadig's Humane Test Output is available (on the classpath), failure reporting is automatically made compatible with it. Expectations that use data structure "equality" (the `=?` extension to `is`) will produce "humane" output for failures, showing differences. #1

# Initial version 1.0.1 -- 2019-01-02
