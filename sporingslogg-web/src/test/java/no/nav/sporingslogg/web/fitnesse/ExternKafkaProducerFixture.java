package no.nav.sporingslogg.web.fitnesse;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.IntegerSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

public class ExternKafkaProducerFixture {
	
	private String bootstrapServers; // TODO noen av disse settes i testen, kunne vært satt i setupServer, eller hardkodet som her
	private String topic;
	private String user = "srvABACPEP";
	private String password = "hUK1.30sKhqp0.(2";
	private String tsFile = "W:/workspace/sporingslogg/sporingslogg-web/src/main/webapp/WEB-INF/local-jetty/nav_truststore_nonproduction_ny2.jts";
	private String tsPassword = "467792be15c4a8807681fd2d5c9c1748";

	KafkaProducer<Integer, String> producer;		

	public ExternKafkaProducerFixture(String bootstrapServers, String topic) {
		this.bootstrapServers = bootstrapServers;
		this.topic = topic;
		Map<String, Object> senderProps = getSenderPropsForExternKafka();
		System.out.println("Bruker Kafka props: " + senderProps);
		System.out.println("################################### Oppretter producer mot server: " + bootstrapServers);	
		try {
			producer = new KafkaProducer<>(senderProps);		
		} catch (Exception e) {
			System.out.println(e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

	public boolean produce(String json) {
		System.out.println("Fikk json: " + json);
		sendMessages(topic, json);
		return true;
	}

	private Map<String, Object> getSenderPropsForExternKafka() {
		Map<String, Object> senderProps = getGeneralSenderProps();
		senderProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);		
		senderProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SASL_SSL.name);
		senderProps.put(SaslConfigs.SASL_JAAS_CONFIG, "org.apache.kafka.common.security.plain.PlainLoginModule required username='"+user
		+"' password='"+password+"';");
		senderProps.put(SaslConfigs.SASL_MECHANISM, "PLAIN");
		senderProps.put(SaslConfigs.SASL_KERBEROS_SERVICE_NAME, "kafka");
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, tsFile);
		senderProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,  tsPassword);
		return senderProps;
	}
	
	private Map<String, Object> getGeneralSenderProps() {
		Map<String, Object> senderProps = new HashMap<>();
		senderProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, IntegerSerializer.class);
		senderProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		return senderProps;
	}
	
	public void sendMessages(String topic, String json) {		
		
		System.out.println("################################### sender meldinger ");		
		try {
			producer.send(lagTestKafkaRecord(json, topic)).get();
		} catch (Exception e) {
			throw new RuntimeException("Kunne ikke sende melding", e);
		} finally {
			producer.close();
		}
		System.out.println("################################### melding sendt, avslutter");	

	}
	private ProducerRecord<Integer, String> lagTestKafkaRecord(String json, String topic) {
		return new ProducerRecord<Integer, String>(topic, json);
	}
}
