# Metodi utili (copiati pari pari) + breve descrizione — Sensor Data Processing

> Raccolta dei metodi principali estratti dai file del progetto, copiati **verbatim** e accompagnati da una descrizione di 2 righe.

---

## DispatcherActor.java

### `supervisorStrategy`
```java
	public SupervisorStrategy supervisorStrategy() {
		return strategy;
	}
```
**Descrizione (2 righe):**  
Strategia di supervisione: definisce come reagire ai fallimenti dei figli (resume/restart/stop).  
Cruciale per il failover: preserva stato valido ed esclude letture difettose.

---

### `props`
```java
	static Props props() {
		return Props.create(DispatcherActor.class);
	}
```
**Descrizione (2 righe):**  
Factory Akka per creare l'attore in modo tipizzato e pulito.  
Utile per l'istanziazione nel main o da altri attori.

---

### `DispatcherActor (costruttore)`
```java
	public DispatcherActor() {
		dispatchMap = new HashMap<ActorRef, ActorRef>();
		processorLoad = new HashMap<ActorRef, Integer>();
		for (int i = 0; i < NO_PROCESSORS; i++) {
			processorLoad.put(getContext().actorOf(SensorProcessorActor.props()), 0);
		}
		nextProcessor = processorLoad.keySet().iterator();
	}
```
**Descrizione (2 righe):**  
Costruttore dell'attore/DTO: inizializza stato e dipendenze interne.  
Punto centrale per wiring di riferimenti e setup di campi necessari.

---

### `loadBalancer`
```java
	private final Receive loadBalancer() {
		return receiveBuilder().match(TemperatureMsg.class, this::dispatchDataLoadBalancer)
				.match(DispatchLogicMsg.class, this::changeDispatcherLogic).build();
	}
```
**Descrizione (2 righe):**  
Metodo del Dispatcher: supporta la distribuzione delle letture verso i processor.  
Adatta questo punto per passare da Round Robin a Load Balancing.

---

### `roundRobin`
```java
	private final Receive roundRobin() {
		return receiveBuilder().match(TemperatureMsg.class, this::dispatchDataRoundRobin)
				.match(DispatchLogicMsg.class, this::changeDispatcherLogic).build();
	}
```
**Descrizione (2 righe):**  
Metodo del Dispatcher: supporta la distribuzione delle letture verso i processor.  
Adatta questo punto per passare da Round Robin a Load Balancing.

---

### `changeDispatcherLogic`
```java
	private void changeDispatcherLogic(DispatchLogicMsg msg) {
		if (msg.getLogic() == DispatchLogicMsg.LOAD_BALANCER) {
			System.out.println("DISPATCHER: Switching to load balancer!");
			getContext().become(loadBalancer());
		} else {
			System.out.println("DISPATCHER: Switching to round robin!");
			getContext().become(roundRobin());
			unstashAll();
		}
	}
```
**Descrizione (2 righe):**  
Metodo del Dispatcher: supporta la distribuzione delle letture verso i processor.  
Adatta questo punto per passare da Round Robin a Load Balancing.

---

### `findLowLoadProcessor`
```java
	private ActorRef findLowLoadProcessor() {

		// Finding the lowest load
		ActorRef lowLoadProcessor = null;
		int lowLoad = Integer.MAX_VALUE;
		for (ActorRef p : processorLoad.keySet()) {
			if (processorLoad.get(p) < lowLoad) {
				lowLoadProcessor = p;
				lowLoad = processorLoad.get(p);
			}
		}

		return lowLoadProcessor;
	}
```
**Descrizione (2 righe):**  
Metodo del Dispatcher: supporta la distribuzione delle letture verso i processor.  
Adatta questo punto per passare da Round Robin a Load Balancing.

---

### `dispatchDataLoadBalancer`
```java
	private void dispatchDataLoadBalancer(TemperatureMsg msg) {

		ActorRef targetProcessor = null;
		if (!dispatchMap.keySet().contains(msg.getSender())) {
			targetProcessor = findLowLoadProcessor();
			processorLoad.put(targetProcessor, processorLoad.get(targetProcessor) + 1);
			dispatchMap.put(msg.getSender(), targetProcessor);
		}
		dispatchMap.get(msg.getSender()).tell(msg, self());
	}
```
**Descrizione (2 righe):**  
Metodo del Dispatcher: supporta la distribuzione delle letture verso i processor.  
Adatta questo punto per passare da Round Robin a Load Balancing.

---

## SensorProcessorActor.java

### `createReceive`
```java
	public Receive createReceive() {
		return receiveBuilder().match(TemperatureMsg.class, this::gotData).build();
	}
```
**Descrizione (2 righe):**  
Definisce il comportamento di ricezione messaggi dell'attore (Akka).  
Qui si mappano i tipi di messaggio ai rispettivi handler/metodi on*.

---

### `props`
```java
	static Props props() {
		return Props.create(SensorProcessorActor.class);
	}
```
**Descrizione (2 righe):**  
Factory Akka per creare l'attore in modo tipizzato e pulito.  
Utile per l'istanziazione nel main o da altri attori.

---

### `SensorProcessorActor (costruttore)`
```java
	public SensorProcessorActor() {
		this.readings = new LinkedList<Integer>();
	}
```
**Descrizione (2 righe):**  
Costruttore dell'attore/DTO: inizializza stato e dipendenze interne.  
Punto centrale per wiring di riferimenti e setup di campi necessari.

---

### `gotData`
```java
	private void gotData(TemperatureMsg msg) throws Exception {
		
		System.out.println("SENSOR PROCESSOR " + self() + ": Got data from " + msg.getSender());

		if (msg.getTemperature()<0) {
			System.out.println("SENSOR PROCESSOR " + self() + ": Failing!");
			throw new Exception("Actor fault!"); 
		}
		
		readings.add(msg.getTemperature());
		int sum = 0;
		for (Integer i : readings) {
			sum = sum + i;
		}
		currentAverage = sum / (double)readings.size();
		System.out.println("SENSOR PROCESSOR " + self() + ": Current avg is " + currentAverage);
	}
```
**Descrizione (2 righe):**  
Metodo del Processor: aggiorna medie e gestisce errori su letture negative.  
Fondamentale per garantire consistenza e failover corretto.

---

## TemperatureSensorActor.java

### `onGenerate`
```java
	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR: Sensing temperature!");
		int temp = ThreadLocalRandom.current().nextInt(MIN_TEMP, MAX_TEMP + 1);
		dispatcher.tell(new TemperatureMsg(temp,self()), self());
	}
```
**Descrizione (2 righe):**  
Genera o gestisce la richiesta di generazione di una nuova lettura del sensore.  
Utile per simulare input continui e testare il flusso end‑to‑end.

---

### `props`
```java
	static Props props() {
		return Props.create(TemperatureSensorActor.class);
	}
```
**Descrizione (2 righe):**  
Factory Akka per creare l'attore in modo tipizzato e pulito.  
Utile per l'istanziazione nel main o da altri attori.

---

### `configure`
```java
	private void configure(ConfigMsg msg) {
		System.out.println("TEMPERATURE SENSOR: Received configuration message!");
		this.dispatcher = msg.getDispatcher();
	}
```
**Descrizione (2 righe):**  
Metodo del Sensore: genera o inoltra letture al dispatcher.  
Permette di simulare carichi e verificare il comportamento di dispatch.

---

## TemperatureSensorFaultyActor.java

### `onGenerate`
```java
	private void onGenerate(GenerateMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Sensing temperature!");
		dispatcher.tell(new TemperatureMsg(FAULT_TEMP,self()), self());
	}
```
**Descrizione (2 righe):**  
Genera o gestisce la richiesta di generazione di una nuova lettura del sensore.  
Utile per simulare input continui e testare il flusso end‑to‑end.

---

### `props`
```java
	static Props props() {
		return Props.create(TemperatureSensorFaultyActor.class);
	}
```
**Descrizione (2 righe):**  
Factory Akka per creare l'attore in modo tipizzato e pulito.  
Utile per l'istanziazione nel main o da altri attori.

---

### `configure`
```java
	private void configure(ConfigMsg msg) {
		System.out.println("TEMPERATURE SENSOR "+self()+": Received configuration message!");
		this.dispatcher = msg.getDispatcher();
	}
```
**Descrizione (2 righe):**  
Metodo del Sensore: genera o inoltra letture al dispatcher.  
Permette di simulare carichi e verificare il comportamento di dispatch.

---

## SensorDataProcessor.java

### `main`
```java
	public static void main(String[] args) {

		final int NO_SENSORS = 4;
		final int SENSING_ROUNDS = 1;
		final ActorSystem sys = ActorSystem.create("System");

		// Create sensor actors
		List<ActorRef> sensors = new LinkedList<ActorRef>();
		for (int i = 0; i < NO_SENSORS; i++) {
			sensors.add(sys.actorOf(TemperatureSensorActor.props(), "t" + i));
		}

		// Create dispatcher
		final ActorRef dispatcher = sys.actorOf(DispatcherActor.props(), "dispatcher");

		// Configure sensors
		for (ActorRef t : sensors) {
			t.tell(new ConfigMsg(dispatcher), ActorRef.noSender());
		}

		// Waiting for configuration messages to arrive
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some temperature data
		for (int i = 0; i < SENSING_ROUNDS; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}

		// Waiting for temperature messages to arrive
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Re-configure dispatcher
		dispatcher.tell(new DispatchLogicMsg(DispatchLogicMsg.ROUND_ROBIN), ActorRef.noSender());

		// Waiting for dispatcher reconfiguration
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some more temperature data
		for (int i = 0; i < 2; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}
		
		// A new (faulty) sensor joins the system
		ActorRef faultySensor = sys.actorOf(TemperatureSensorFaultyActor.props(), "tFaulty");
		faultySensor.tell(new ConfigMsg(dispatcher), ActorRef.noSender());
		sensors.add(0, faultySensor);
		// Waiting for configuration message to arrive
		try {
			TimeUnit.SECONDS.sleep(1);
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		// Generate some more temperature data
		for (int i = 0; i < 2; i++) {
			for (ActorRef t : sensors) {
				t.tell(new GenerateMsg(), ActorRef.noSender());
			}
		}
	}
```
**Descrizione (2 righe):**  
Metodo del Processor: aggiorna medie e gestisce errori su letture negative.  
Fondamentale per garantire consistenza e failover corretto.

---

## ConfigMsg.java

### `ConfigMsg (costruttore)`
```java
    public ConfigMsg (ActorRef dispatcher) {
    	this.dispatcher = dispatcher;
    }
```
**Descrizione (2 righe):**  
Costruttore dell'attore/DTO: inizializza stato e dipendenze interne.  
Punto centrale per wiring di riferimenti e setup di campi necessari.

---

### `getDispatcher`
```java
	public ActorRef getDispatcher() {
		return dispatcher;
	}
```
**Descrizione (2 righe):**  
Accessor/mutator di un messaggio/DTO: incapsula campi come topic, valore o logica.  
Comodo per costruire pipeline di messaggi tipizzati e leggibili.

---

## DispatchLogicMsg.java

### `DispatchLogicMsg (costruttore)`
```java
    public DispatchLogicMsg (int logic) {
    	this.logic = logic;
    }
```
**Descrizione (2 righe):**  
Costruttore dell'attore/DTO: inizializza stato e dipendenze interne.  
Punto centrale per wiring di riferimenti e setup di campi necessari.

---

### `getLogic`
```java
	public int getLogic() {
		return logic;
	}
```
**Descrizione (2 righe):**  
Accessor/mutator di un messaggio/DTO: incapsula campi come topic, valore o logica.  
Comodo per costruire pipeline di messaggi tipizzati e leggibili.

---

## TemperatureMsg.java

### `TemperatureMsg (costruttore)`
```java
	public TemperatureMsg(int temp, ActorRef sender) {
		this.temperature = temp;
		this.sender = sender;
	}
```
**Descrizione (2 righe):**  
Costruttore dell'attore/DTO: inizializza stato e dipendenze interne.  
Punto centrale per wiring di riferimenti e setup di campi necessari.

---

### `getTemperature`
```java
	public int getTemperature() {
		return temperature;
	}
```
**Descrizione (2 righe):**  
Accessor/mutator di un messaggio/DTO: incapsula campi come topic, valore o logica.  
Comodo per costruire pipeline di messaggi tipizzati e leggibili.

---

### `getSender`
```java
	public ActorRef getSender() {
		return sender;
	}
```
**Descrizione (2 righe):**  
Accessor/mutator di un messaggio/DTO: incapsula campi come topic, valore o logica.  
Comodo per costruire pipeline di messaggi tipizzati e leggibili.

---

## Note su Failover & Stato (riuso veloce)

- **Strategia di supervisione**: usa `resume()` quando vuoi **preservare lo stato** del processor (media) ed **ignorare** la lettura che ha causato l'errore; usa `restart()` quando vuoi **ricostruire lo stato** da una snapshot sicura.

- **Hook di restart**: override di `preRestart`/`postRestart` per **salvare/ripristinare** media e conteggi. Escludi esplicitamente la lettura negativa per rispettare il requisito dell'assegnamento.

- **Idempotenza**: quando ricevi la stessa lettura dopo un recover, controlla un ID o timestamp per evitare doppi conteggi nella media.
