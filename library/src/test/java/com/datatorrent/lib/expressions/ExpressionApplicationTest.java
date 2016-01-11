/**
 * Copyright (c) 2016 DataTorrent, Inc.
 * All rights reserved.
 */
package com.datatorrent.lib.expressions;

import com.datatorrent.api.*;
import com.datatorrent.common.util.BaseOperator;
import org.apache.hadoop.conf.Configuration;
import org.junit.Assert;
import org.junit.Test;

import javax.validation.ConstraintViolationException;

public class ExpressionApplicationTest
{
  @Test public void testExpressionApplication() throws Exception
  {
    try {
      LocalMode lma = LocalMode.newInstance();
      Configuration conf = new Configuration(false);
      lma.prepareDAG(new ExpressionApplication(), conf);
      LocalMode.Controller lc = lma.getController();
      lc.run(10000); // runs for 10 seconds and quits
    } catch (ConstraintViolationException e) {
      Assert.fail("constraint violations: " + e.getConstraintViolations());
    }
  }

  public static class ExpressionApplication implements StreamingApplication
  {
    @Override public void populateDAG(DAG dag, Configuration configuration)
    {
      String exp = "upperCase(${" + DummyProcessor.VARIABLE_PLACEHOLDER + ".firstname}) + " +
          "\" \" + " +
          "upperCase(${" + DummyProcessor.VARIABLE_PLACEHOLDER + ".lastname})";

      DummyInputGenerator input = dag.addOperator("Input", new DummyInputGenerator());
      DummyProcessor processor = dag.addOperator("Processor", new DummyProcessor());
      processor.setStringExp(exp);

      dag.addStream("Connect", input.output, processor.input);
    }
  }

  public static class TestPojo
  {
    private String firstname;
    public String lastname;

    public TestPojo()
    {
      //for kryo
    }

    public TestPojo(String firstname, String lastname)
    {
      this.firstname = firstname;
      this.lastname = lastname;
    }

    public String getFirstname()
    {
      return firstname;
    }

    public void setFirstname(String firstname)
    {
      this.firstname = firstname;
    }
  }

  public static class DummyInputGenerator implements InputOperator
  {
    public final transient DefaultOutputPort<TestPojo> output = new DefaultOutputPort<>();

    @Override public void emitTuples()
    {
      output.emit(new TestPojo("FirstName", "LastName"));
    }

    @Override public void beginWindow(long l)
    {
    }

    @Override public void endWindow()
    {
    }

    @Override public void setup(Context.OperatorContext context)
    {
    }

    @Override public void teardown()
    {
    }
  }

  public static class DummyProcessor extends BaseOperator
  {
    public static String VARIABLE_PLACEHOLDER = "inp";

    private String stringExp;
    private ExpressionEvaluator ee;
    private ExpressionEvaluator.Expression expression;

    public final transient DefaultInputPort<TestPojo> input = new DefaultInputPort<TestPojo>()
    {
      @Override public void process(TestPojo testPojo)
      {
        Object result = expression.execute(testPojo);
        Assert.assertTrue(result instanceof String);
        String result1 = (String)result;
        Assert.assertEquals("FIRSTNAME LASTNAME", result1);
      }
    };

    @Override public void setup(Context.OperatorContext context)
    {
      ee = new ExpressionEvaluator();
      ee.setInputObjectPlaceholders(new String[] { VARIABLE_PLACEHOLDER }, new Class[] { TestPojo.class });
      expression = ee.createExecutableExpression(stringExp, String.class);
    }

    public String getStringExp()
    {
      return stringExp;
    }

    public void setStringExp(String stringExp)
    {
      this.stringExp = stringExp;
    }
  }
}
