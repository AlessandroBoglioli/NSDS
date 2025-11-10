# Metodi utili (copiati pari pari) + breve descrizione

> Raccolta dei metodi principali estratti dalla tua soluzione, copiati **verbatim** e accompagnati da una descrizione di 2 righe per l'uso futuro in assignment simili.

---

## BrokerActor.java

### `public SupervisorStrategy supervisorStrategy()`
```java
@Override
public SupervisorStrategy supervisorStrategy() {
    return strategy;
}
```
**Descrizione (2 righe):**  
Ritorna la `SupervisorStrategy` che usa `resume()` sugli errori, preservando lo stato dei figli (es. mappe di subscription).  
È fondamentale per garantire fault-tolerance senza perdere le sottoscrizioni ai worker dopo i fallimenti intenzionali.

---

### `public AbstractActor.Receive createReceive()`
```java
@Override
public AbstractActor.Receive createReceive() {
    return batchedOff();
}
```
**Descrizione (2 righe):**  
Imposta il comportamento di default del broker quando il batching è disattivato.  
Punto d’ingresso del message handling che delega agli handler nello stato “batchedOff”.

---

### `private final Receive batchedOff()`
```java
private final Receive batchedOff() {
    return receiveBuilder()
            .match(SubscribeMsg.class, this::onSubscribe)
            .match(PublishMsg.class, this::onPublish)
            .match(BatchMsg.class, this::onBatching).build();
}
```
**Descrizione (2 righe):**  
Stato “normale”: processa subito subscribe/publish e gestisce l’interruttore di batching.  
Usa `onSubscribe`, `onPublish` e `onBatching` per la logica operativa del broker.

---

### `private final Receive batchedOn()`
```java
private final Receive batchedOn() {
    return receiveBuilder() // TODO change
            .match(SubscribeMsg.class, this::onSubscribe)
            .match(PublishMsg.class, this::onPubBatch)
            .match(BatchMsg.class, this::onBatching).build();
}
```
**Descrizione (2 righe):**  
Stato con batching attivo: le `PublishMsg` vengono stivate tramite `onPubBatch`.  
Consente di accumulare eventi e processarli solo al successivo `BatchMsg(false)`.

---

### Costruttore `public BrokerActor()`
```java
public BrokerActor() {
    workerEven = getContext().actorOf(WorkerActor.props());
    workerOdd = getContext().actorOf(WorkerActor.props());
}
```
**Descrizione (2 righe):**  
Crea i due worker figli (pari e dispari) per partizionare il carico.  
Wiring fisso: utile per avere subito i riferimenti ai worker nel broker.

---

### `private void onSubscribe(SubscribeMsg msg)`
```java
private void onSubscribe(SubscribeMsg msg) {
    if (msg.getKey() % 2== 1) {
        System.out.println("BROKER: Subscribed to odd worker");
        workerOdd.tell(msg, self());
    } else {
        System.out.println("BROKER: Subscribed to even worker");
        workerEven.tell(msg, self());
    }
}
```
**Descrizione (2 righe):**  
Partiziona le sottoscrizioni su base chiave: dispari → `workerOdd`, pari → `workerEven`.  
Implementa lo sharding semplice richiesto dall’assignment.

---

### `private void onPublish(PublishMsg msg)`
```java
private void onPublish(PublishMsg msg) {
    workerOdd.tell(msg, self());
    workerEven.tell(msg, self());
}
```
**Descrizione (2 righe):**  
Inoltra il publish ad entrambi i worker (broadcast).  
Semplice e robusto: ogni worker notifica solo se ha una subscription per quel topic.

---

### `private void onBatching(BatchMsg msg)`
```java
private void onBatching(BatchMsg msg) {
    if (msg.isOn()) {
        System.out.println("BROKER: batching turned on");
        getContext().become(batchedOn());
    } else {
        System.out.println("BROKER: batching turned off");
        getContext().become(batchedOff());
        unstashAll();
    }
}
```
**Descrizione (2 righe):**  
Accende/spegne la modalità batching usando `become(...)`.  
Quando si spegne, esegue `unstashAll()` per drenare e processare gli eventi accumulati.

---

### `private void onPubBatch(PublishMsg msg)`
```java
private void onPubBatch(PublishMsg msg) {
    System.out.println("BROKER: publish message stashed");
    stash();
}
```
**Descrizione (2 righe):**  
Stiva i messaggi `PublishMsg` quando il batching è attivo.  
Base per implementare “buffer-then-flush” controllato via `BatchMsg`.

---

### `static Props props()`
```java
static Props props() {
    return Props.create(BrokerActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard Akka per creare l’attore.  
Mantiene il codice di creazione pulito nel `main` o in altri attori.

---

## WorkerActor.java

### `public Receive createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(SubscribeMsg.class, this::onSubscribe)
            .match(PublishMsg.class, this::onPublish).build();
}
```
**Descrizione (2 righe):**  
Definisce gli handler di subscribe e publish per il worker.  
È la tabella di routing dei messaggi gestiti dal worker.

---

### `private void onSubscribe(SubscribeMsg msg)`
```java
private void onSubscribe(SubscribeMsg msg) {
    subscriptions.put(msg.getTopic(), msg.getSender());
}
```
**Descrizione (2 righe):**  
Registra la sottoscrizione mappando `topic → subscriber`.  
Costruisce lo stato locale necessario al matching eventi→subscriber.

---

### `private void onPublish(PublishMsg msg) throws Exception`
```java
private void onPublish(PublishMsg msg) throws Exception {
    // If the topic doesn't exist on this worker's map
    if (!subscriptions.containsKey(msg.getTopic()))
        throw new Exception("Topic not registered " + getContext().getSelf().toString());

    subscriptions.get(msg.getTopic()).tell(new NotifyMsg(msg.getValue()), self());
}
```
**Descrizione (2 righe):**  
Se il topic non è registrato, fallisce intenzionalmente (triggerando la supervisione).  
Altrimenti notifica il subscriber con `NotifyMsg` contenente il valore dell’evento.

---

### `static Props props()`
```java
static Props props() {
    return Props.create(WorkerActor.class);
}
```
**Descrizione (2 righe):**  
Factory per creare un `WorkerActor`.  
Uniforma la creazione e facilita i test.

---

## SubscriberActor.java

### `public AbstractActor.Receive createReceive()`
```java
@Override
public AbstractActor.Receive createReceive() {
    return receiveBuilder()
            .match(ConfigMsg.class, this::configure)
            .match(SubscribeMsg.class, this::onSubscribe)
            .match(NotifyMsg.class, this::onNotify).build();
}
```
**Descrizione (2 righe):**  
Gestisce configurazione, comandi di subscribe e notifiche ricevute.  
È la superficie API del subscriber verso l’esterno.

---

### `private void configure(ConfigMsg msg)`
```java
private void configure(ConfigMsg msg) {
    System.out.println("SUBSCRIBER: Received configuration message!");
    broker = msg.getBrokerRef();
}
```
**Descrizione (2 righe):**  
Inietta il riferimento al broker nel subscriber.  
Pattern pulito per evitare dipendenze nel costruttore.

---

### `private void onSubscribe(SubscribeMsg msg)`
```java
private void onSubscribe(SubscribeMsg msg) {
    System.out.println("SUBSCRIBER: Received subscribe command!");
    broker.tell(msg, self());
}
```
**Descrizione (2 righe):**  
Inoltra la richiesta di sottoscrizione al broker.  
Separazione chiara tra input “utente” e messaggi di sistema.

---

### `private void onNotify(NotifyMsg msg)`
```java
private void onNotify(NotifyMsg msg) {
    System.out.println("SUBSCRIBER: Received notify message!");
    System.out.println("Received value: " + msg.getValue());
}
```
**Descrizione (2 righe):**  
Gestisce la ricezione delle notifiche su eventi.  
Punto di estensione naturale per logiche applicative (aggiornamento UI, persistenza, ecc.).

---

### `static Props props()`
```java
static Props props() {
    return Props.create(SubscriberActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per creare il `SubscriberActor`.  
Mantiene coerente la creazione in tutto il progetto.

---

## PublisherActor.java

### `public AbstractActor.Receive createReceive()`
```java
@Override
public AbstractActor.Receive createReceive() {
    return receiveBuilder()
            .match(ConfigMsg.class, this::configure)
            .match(PublishMsg.class, this::onPublish).build();
}
```
**Descrizione (2 righe):**  
Gestisce configurazione e comandi di publish.  
Interfaccia principale del publisher con il resto del sistema.

---

### `private void configure(ConfigMsg msg)`
```java
private void configure(ConfigMsg msg) {
    System.out.println("PUBLISHER: Received configuration message!");
    broker = msg.getBrokerRef();
}
```
**Descrizione (2 righe):**  
Imposta il riferimento al broker all’interno del publisher.  
Permette di cambiare wiring senza ricreare l’attore.

---

### `private void onPublish(PublishMsg msg)`
```java
private void onPublish(PublishMsg msg) {
    System.out.println("PUBLISHER: Received publish command!");
    broker.tell(msg, self());
}
```
**Descrizione (2 righe):**  
Inoltra il messaggio di pubblicazione al broker.  
Punto ideale per aggiungere in futuro rate-limiting o metadati.

---

### `static Props props()`
```java
static Props props() {
    return Props.create(PublisherActor.class);
}
```
**Descrizione (2 righe):**  
Factory per creare il `PublisherActor`.  
Semplifica la creazione nel `main` e nei test.

---

## Messaggi (costruttori e getter)

### ConfigMsg
```java
public ConfigMsg (ActorRef broker) {
    this.broker = broker;
}

public ActorRef getBrokerRef() {
    return broker;
}
```
**Descrizione (2 righe):**  
Trasporta il riferimento al broker per configurare publisher/subscriber.  
Semplice DTO di wiring con solo costruttore e getter.

---

### SubscribeMsg
```java
public SubscribeMsg (String topic, ActorRef sender) {
    this.key = this.hashCode();
    this.topic = topic;
    this.sender = sender;
}

public String getTopic() {
    return topic;
}

public int getKey() {
    return key;
}

public ActorRef getSender() {
    return sender;
}
```
**Descrizione (2 righe):**  
Incapsula i dati di una sottoscrizione: topic, chiave (derivata da `hashCode`) e riferimento del subscriber.  
La chiave è usata dal broker per partizionare pari/dispari.

---

### PublishMsg
```java
public PublishMsg (String topic, String value) {
    this.topic = topic;
    this.value = value;
}

public String getTopic() {
    return topic;
}

public String getValue() {
    return value;
}
```
**Descrizione (2 righe):**  
Rappresenta un evento pubblicato con `topic` e `value`.  
È il payload principale che viaggia dal publisher al broker (e poi ai worker).

---

### NotifyMsg
```java
public NotifyMsg (String value) {
    this.value = value;
}

public String getValue() {
    return value;
}
```
**Descrizione (2 righe):**  
Messaggio di notifica inviato dal worker al subscriber.  
Trasporta il valore dell’evento che ha fatto match con la subscription.

---

### BatchMsg
```java
public BatchMsg(boolean isOn) {
    this.isOn = isOn;
}

public boolean isOn() {
    return isOn;
}
```
**Descrizione (2 righe):**  
Interruttore per attivare/disattivare la modalità di batching nel broker.  
Con `true` si accoda; con `false` si processa immediatamente e si fa flush.

---

## (Bonus) PubSub.java — `main(...)`

> Utile come “scaffold” per test rapidi.

```java
public static void main(String[] args) {

    final ActorSystem sys = ActorSystem.create("System");
    final ActorRef broker = sys.actorOf(BrokerActor.props(), "broker");
    final ActorRef subscriber = sys.actorOf(SubscriberActor.props(), "subscriber");
    final ActorRef publisher = sys.actorOf(PublisherActor.props(), "publisher");

    // Tell the actors who the broker is
    subscriber.tell(new ConfigMsg(broker), ActorRef.noSender());
    publisher.tell(new ConfigMsg(broker), ActorRef.noSender());

    // Some example subscriptions
    subscriber.tell(new SubscribeMsg(TOPIC0, subscriber), ActorRef.noSender());
    subscriber.tell(new SubscribeMsg(TOPIC1, subscriber), ActorRef.noSender());
            
    // Waiting for subscriptions to propagate
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }
    
    // Some example events        
    publisher.tell(new PublishMsg(TOPIC0, "Test event 1"), ActorRef.noSender());
    publisher.tell(new PublishMsg(TOPIC1, "Test event 2"), ActorRef.noSender());
    
    // Waiting for events to propagate
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }

    // Turn the broker in batching mode
    broker.tell(new BatchMsg(true), ActorRef.noSender());

    // Waiting for messages to propagate
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }

    // More example events
    publisher.tell(new PublishMsg(TOPIC0, "Test message 3"), ActorRef.noSender());
    publisher.tell(new PublishMsg(TOPIC1, "Test message 4"), ActorRef.noSender());
    
    // Waiting for events to propagate
    try {
        TimeUnit.SECONDS.sleep(1);
    } catch (InterruptedException e1) {
        // TODO Auto-generated catch block
        e1.printStackTrace();
    }

    broker.tell(new BatchMsg(false), ActorRef.noSender());
    // In this example, the last two events shall not be processed until after this point

    // Wait for all messages to be sent and received
    try {
        System.in.read();
    } catch (IOException e) {
        e.printStackTrace();
    }
    sys.terminate();
}
```
**Descrizione (2 righe):**  
Esegue wiring base, invia configurazioni, subscribe, publish e dimostra il comportamento del batching.  
Perfetto come template per smoke test o per inizializzare demo locali.

---

_Fine._
