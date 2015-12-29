/**
 * Copyright (c) 2015 DataTorrent, Inc.
 * All rights reserved.
 */
package com.datatorrent.lib.expressions;

import java.util.Date;
import com.datatorrent.lib.expressions.ExpressionEvaluatorTest.POJO1;
import com.datatorrent.lib.expressions.ExpressionEvaluatorTest.POJO2;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExpressionPerformanceTest
{
  private static final Logger logger = LoggerFactory.getLogger(ExpressionPerformanceTest.class);

  @Test
  public void testExpressionPerformance() throws Exception
  {
    logger.info("===Expression performance tests===");
    logger.info("---1 million---");
    runExpressionPerformance(1000000);
    logger.info("---10 million---");
    runExpressionPerformance(10000000);
    logger.info("---100 million---");
    runExpressionPerformance(100000000);
    logger.info("---1 billion---");
    runExpressionPerformance(1000000000);
  }

  @Test
  public void testFunctionPerformance() throws Exception
  {
    logger.info("===Function performance tests===");
    logger.info("---1 million---");
    runFunctionPerformance(1000000);
    logger.info("---10 million---");
    runFunctionPerformance(10000000);
  }

  @Test
  public void testMathExpressionPerformance() throws Exception
  {
    logger.info("===Math Expression performance tests===");
    logger.info("---1 million---");
    runMathExpressionPerformance(1000000);
    logger.info("---10 million---");
    runMathExpressionPerformance(10000000);
  }

  public void runMathExpressionPerformance(int count) throws Exception
  {
    ExpressionEvaluator ee = new ExpressionEvaluator();
    // Let expression evaluator know what are the object mappings present in expressions and their class types.
    ee.setInputObjectPlaceholders(new String[]{"inp"}, new Class[]{Double.class});

    String expression1 = "2 + (7-5) * 3.14159 * ${inp} + sin(${inp})";
    ExpressionEvaluator.Expression getter = ee.createExecutableExpression(expression1, double.class);

    long startTime = System.currentTimeMillis();
    for (int i=0; i < count; i++) {
      getter.execute((double) i);
    }
    long timeTaken = (System.currentTimeMillis() - startTime);
    logger.info("Time taken for running " + count + " executions in ms: " + timeTaken);
    logger.info("Executions per sec: " + ((long)count * 1000 / timeTaken));
  }

  public void runExpressionPerformance(int count) throws Exception
  {
    ExpressionEvaluator ee = new ExpressionEvaluator();
    // Let expression evaluator know what are the object mappings present in expressions and their class types.
    ee.setInputObjectPlaceholders(new String[]{"inpA", "inpB"}, new Class[]{POJO1.class, POJO2.class});

    String expression1 = "equalsWithCase(${inpA.name1}, ${inpA.name2}) ? ${inpB.a} * ${inpB.a} : toInt(${inpB.b}) * toInt(${inpB.b})";
    ExpressionEvaluator.Expression<Integer> getter = ee.createExecutableExpression(expression1, Integer.class);

    POJO1 testPOJO11 = createTestPOJO1();
    POJO2 testPOJO2 = createTestPOJO2();

    POJO1 testPOJO12 = createTestPOJO1();
    testPOJO12.name1 = "DataTorrent";

    long startTime = System.currentTimeMillis();
    for (int i=0; i < count; i++) {
      if ( i%2 == 0) {
        getter.execute(testPOJO11, testPOJO2);
      }
      else {
        getter.execute(testPOJO12, testPOJO2);
      }
    }
    long timeTaken = (System.currentTimeMillis() - startTime);
    logger.info("Time taken for running " + count + " executions in ms: " + timeTaken);
    logger.info("Executions per sec: " + ((long)count * 1000 / timeTaken));
  }

  public void runFunctionPerformance(int count) throws Exception
  {
    ExpressionEvaluator ee = new ExpressionEvaluator();
    // Let expression evaluator know what are the object mappings present in expressions and their class types.
    ee.setInputObjectPlaceholders(new String[]{"inpA", "inpB"}, new Class[]{POJO1.class, POJO2.class});

    String expression1 = "long retVal = 1; " +
                          "if (equalsWithCase(${inpA.name1}, ${inpA.name2})) { " +
                          "  for (int i=0;i< ${inpA.a}; i++) {" +
                          "    retVal = retVal * ${inpA.a};" +
                          "  }" +
                          "} else {" +
                          "  for (int i=0;i<${inpB.a};i++) {" +
                          "    retVal = retVal * ${inpB.a};" +
                          "  }" +
                          "} " +
                          "return retVal;";
    ExpressionEvaluator.Expression<Long> getter = ee.createExecutableFunction(expression1, Long.class);

    POJO1 testPOJO11 = createTestPOJO1();
    POJO2 testPOJO2 = createTestPOJO2();

    POJO1 testPOJO12 = createTestPOJO1();
    testPOJO12.name1 = "DataTorrent";

    long startTime = System.currentTimeMillis();
    for (int i=0; i < count; i++) {
      if ( i%2 == 0) {
        getter.execute(testPOJO11, testPOJO2);
      }
      else {
        getter.execute(testPOJO12, testPOJO2);
      }
    }
    long timeTaken = (System.currentTimeMillis() - startTime);
    logger.info("Time taken for running " + count + " executions in ms: " + timeTaken);
    logger.info("Executions per sec: " + ((long)count * 1000 / timeTaken));
  }

  private POJO1 createTestPOJO1()
  {
    POJO1 pojo = new POJO1();
    pojo.setA(12);
    pojo.setB(13);
    pojo.setD(new Date(1988 - 1900, 2, 11));
    pojo.name1 = "Apex";
    pojo.name2 = "DataTorrent";

    return pojo;
  }

  private POJO2 createTestPOJO2()
  {
    POJO2 pojo = new POJO2();
    pojo.setA(1234);
    pojo.setB(1234.56D);
    return pojo;
  }

}
