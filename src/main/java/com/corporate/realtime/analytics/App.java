/*
 * This Java source file was generated by the Gradle 'init' task.
 */
package com.corporate.realtime.analytics;

import java.io.PrintStream;
import java.util.Properties;

import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.api.common.functions.FilterFunction;
import org.apache.flink.api.common.functions.MapFunction;

import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.datastream.DataStream;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.api.common.serialization.Encoder;
import org.apache.flink.api.java.utils.ParameterTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class App {

    private static final Logger LOG = LoggerFactory.getLogger(App.class);

    public String getGreeting() {
        return "Hello World!";
    }

    public static void main(String[] args) throws Exception {
        System.out.println(new App().getGreeting());

		final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();

        final ParameterTool params = ParameterTool.fromArgs(args);

        env.getConfig().setGlobalJobParameters(params);

        DataStream<String> text = env.socketTextStream("localhost",9999);

        DataStream<Tuple2<Integer,String>> datastream1 = text.map(
            new MapFunction<String, Tuple2<Integer,String>>()
            {
                public Tuple2<Integer,String> map(String value){
                    String[] words = value.split(",");
                    return new Tuple2<Integer, String>(Integer.parseInt(words[0]),words[1]);
                }
            }
        );

        DataStream<String> streamFiltered = datastream1.filter(
            new FilterFunction<Tuple2<Integer,String>>()
            {
                public boolean filter(Tuple2<Integer,String> tuple){
                    return tuple.f1.startsWith("N");
                }
            }
        );

        DataStream<Tuple3<Integer, String, Integer>> tokenized = streamFiltered.map(new Tokenizer());

        DataStream<Tuple3<Integer, String,Integer>> counts = tokenized.keyBy(1).sum(2);

        counts.print();

        env.execute("Stream WorkCount");
    }

    public static final class Tokenizer implements MapFunction<Tuple2<Integer,String>, Tuple3<Integer,String, Integer>>
    {
        @Override
        public Tuple2<String, Integer> map(Tuple2<Integer,String> tuple){
            return new Tuple3<Integer, String, Integer>(tuple.f0, tuple.f1, 1);
        }
    }

}
