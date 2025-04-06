package com.epam.training.gen.ai.examples.semantic.plugin;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.Random;

/**
 * A simple plugin that defines a kernel function for performing multiplication of two random ints.
 * <p>
 * This plugin exposes a method to be invoked by the kernel, which logs and returns the input query.
 */
@Slf4j
public class MultiplyPlugin {

    @DefineKernelFunction(name = "multiplyAction", description = "Makes a multiplication on two random integers.")
    public Integer getBingSearchUrl() {
        Random random = new Random();
        Integer int1 = random.ints(1, 9)
                .findFirst()
                .getAsInt();
        Integer int2 = random.ints(1, 9)
                .findFirst()
                .getAsInt();
        Integer result = int1 * int2;
        log.info("MultiplyPlugin: {} * {} = {}", int1, int2, result);
        return result;
    }
}
