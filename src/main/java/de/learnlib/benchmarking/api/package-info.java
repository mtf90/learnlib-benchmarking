/**
 * This package (including sub-packages) contain the abstract endpoint definitions for the benchmarking service. We need
 * to split between api (interfaces) and {@link de.learnlib.benchmarking.impl impl} (implementation) because we want to
 * automatically generated HTTP clients for testing which requires the endpoint definitions to be interfaces.
 */

package de.learnlib.benchmarking.api;