package de.learnlib.benchmarking.dto;

import java.util.List;

import de.learnlib.cli.option.EQOParams;
import de.learnlib.cli.option.EQOracle;

public record EQOConfig(List<EQOracle> oracles, EQOParams params) {}
