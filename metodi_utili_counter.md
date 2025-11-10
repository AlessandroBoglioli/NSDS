# Metodi utili (copiati pari pari) + breve descrizione — Counter, AddressBook e SleepyEcho (Esercizi 1–4)

> Metodi estratti dalle tue soluzioni, copiati **verbatim** e accompagnati da una descrizione di 2 righe. Ho riunito **tutti** gli esercizi nello stesso documento.

---

## COUNTER — Esercizi 1 e 2

### Counter.java

#### `Counter (costruttore)`
```java
public Counter() {
    this.value = 0;
}
```
**Descrizione (2 righe):**  
Inizializza il contatore a zero alla creazione.  
Punto di partenza per la gestione dello stato numerico.

---

#### `public void increment()`
```java
public void increment() {
    value++;
}
```
**Descrizione (2 righe):**  
Incrementa il valore del contatore di una unità.  
Metodo base per la logica di incremento nei test Akka.

---

#### `public void decrement()`
```java
public void decrement() {
    value--;
}
```
**Descrizione (2 righe):**  
Riduce il valore del contatore di una unità.  
Utile per testare il comportamento di decremento e validazioni.

---

#### `public int getValue()`
```java
public int getValue() {
    return value;
}
```
**Descrizione (2 righe):**  
Restituisce il valore corrente del contatore.  
Accessor utile per verificare il corretto aggiornamento dello stato.

---

### CounterActorIncrementDecrement.java

#### `CounterActorIncrementDecrement (costruttore)`
```java
public CounterActorIncrementDecrement() {
    counter = new Counter();
}
```
**Descrizione (2 righe):**  
Inizializza l’attore creando un nuovo oggetto Counter.  
Collega la logica di stato all’attore Akka.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(IncrementMessage.class, this::onIncrement)
            .match(DecrementMessage.class, this::onDecrement)
            .match(SimpleMessage.class, this::onSimple)
            .build();
}
```
**Descrizione (2 righe):**  
Definisce come l’attore risponde ai diversi tipi di messaggio.  
Gestisce incremento, decremento e messaggi generici SimpleMessage.

---

#### `onIncrement(IncrementMessage msg)`
```java
private void onIncrement(IncrementMessage msg) {
    counter.increment();
    System.out.println("Counter incremented: " + counter.getValue());
}
```
**Descrizione (2 righe):**  
Gestisce il messaggio di incremento aggiornando lo stato del contatore.  
Mostra a video il valore aggiornato per debug.

---

#### `onDecrement(DecrementMessage msg)`
```java
private void onDecrement(DecrementMessage msg) {
    counter.decrement();
    System.out.println("Counter decremented: " + counter.getValue());
}
```
**Descrizione (2 righe):**  
Gestisce il messaggio di decremento aggiornando lo stato del contatore.  
Stampa il nuovo valore utile per test e monitoraggio.

---

#### `onSimple(SimpleMessage msg)`
```java
private void onSimple(SimpleMessage msg) {
    System.out.println("Received simple message: " + msg.getContent());
}
```
**Descrizione (2 righe):**  
Gestisce i messaggi di tipo SimpleMessage stampando il contenuto.  
Utile per testare la ricezione e logging generico.

---

#### `props()`
```java
static Props props() {
    return Props.create(CounterActorIncrementDecrement.class);
}
```
**Descrizione (2 righe):**  
Factory per creare l’attore in modo tipizzato.  
Facilita la creazione in test e in altri attori.

---

### SimpleMessage.java (Esercizio 2)

#### `SimpleMessage (costruttore)`
```java
public SimpleMessage(String content) {
    this.content = content;
}
```
**Descrizione (2 righe):**  
Costruttore del messaggio semplice, inizializza il campo testo.  
Serve come modello di messaggio base per test di ricezione.

---

#### `getContent()`
```java
public String getContent() {
    return content;
}
```
**Descrizione (2 righe):**  
Restituisce il contenuto testuale del messaggio.  
Accessor utile per estrarre il testo ricevuto negli handler Akka.

---

## ADDRESS BOOK — Esercizio 3

### AddressBook.java

#### `AddressBook (costruttore)`
```java
public AddressBook() {
    entries = new HashMap<>();
}
```
**Descrizione (2 righe):**  
Inizializza la rubrica creando una nuova mappa nome→indirizzo.  
Costruttore di base per lo stato locale dei dati.

---

#### `public void put(String name, String address)`
```java
public void put(String name, String address) {
    entries.put(name, address);
}
```
**Descrizione (2 righe):**  
Inserisce o aggiorna un contatto nella rubrica.  
Metodo principale per la gestione dello stato nella rubrica.

---

#### `public String get(String name)`
```java
public String get(String name) {
    return entries.get(name);
}
```
**Descrizione (2 righe):**  
Restituisce l’indirizzo associato a un nome.  
Accessor utile per rispondere alle richieste Get.

---

### AddressBookServerActor.java

#### `AddressBookServerActor (costruttore)`
```java
public AddressBookServerActor() {
    addressBook = new AddressBook();
}
```
**Descrizione (2 righe):**  
Crea un’istanza di rubrica all’interno dell’attore server.  
Punto iniziale per la gestione dei dati condivisi.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(PutMsg.class, this::onPut)
            .match(GetMsg.class, this::onGet)
            .build();
}
```
**Descrizione (2 righe):**  
Definisce la logica di gestione dei messaggi Put e Get.  
Stabilisce il comportamento dell’attore server per la rubrica.

---

#### `onPut(PutMsg msg)`
```java
private void onPut(PutMsg msg) {
    addressBook.put(msg.getName(), msg.getAddress());
}
```
**Descrizione (2 righe):**  
Gestisce l’inserimento o aggiornamento di un contatto nella rubrica.  
Esegue l’operazione Put richiesta dal client.

---

#### `onGet(GetMsg msg)`
```java
private void onGet(GetMsg msg) {
    String address = addressBook.get(msg.getName());
    getSender().tell(new ReplyMsg(address), getSelf());
}
```
**Descrizione (2 righe):**  
Gestisce la richiesta di recupero indirizzo inviando `ReplyMsg`.  
Implementa la comunicazione client-server Akka.

---

#### `props()`
```java
static Props props() {
    return Props.create(AddressBookServerActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per creare un `AddressBookServerActor`.  
Permette l’istanza nel main o in altri componenti.

---

### AddressBookClientActor.java

#### `AddressBookClientActor (costruttore)`
```java
public AddressBookClientActor() {}
```
**Descrizione (2 righe):**  
Costruttore vuoto del client, pronto per ricevere la configurazione del server.  
Consente di inizializzare l’attore prima della connessione.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(ConfigMsg.class, this::configure)
            .match(PutMsg.class, this::onPut)
            .match(GetMsg.class, this::onGet)
            .match(ReplyMsg.class, this::onReply)
            .build();
}
```
**Descrizione (2 righe):**  
Definisce come il client reagisce a Config, Put, Get e Reply.  
Gestisce le interazioni tra utente e server della rubrica.

---

#### `configure(ConfigMsg msg)`
```java
private void configure(ConfigMsg msg) {
    server = msg.getServerRef();
}
```
**Descrizione (2 righe):**  
Imposta il riferimento dell’attore server per la comunicazione.  
Pattern standard di configurazione per client Akka.

---

#### `onPut(PutMsg msg)`
```java
private void onPut(PutMsg msg) {
    server.tell(msg, getSelf());
}
```
**Descrizione (2 righe):**  
Invia al server un messaggio Put per aggiungere o aggiornare un contatto.  
Gestisce l’invio di richieste di scrittura.

---

#### `onGet(GetMsg msg)`
```java
private void onGet(GetMsg msg) {
    server.tell(msg, getSelf());
}
```
**Descrizione (2 righe):**  
Inoltra al server una richiesta Get per ottenere un indirizzo.  
Responsabile della comunicazione client→server per letture.

---

#### `onReply(ReplyMsg msg)`
```java
private void onReply(ReplyMsg msg) {
    System.out.println("Address received: " + msg.getAddress());
}
```
**Descrizione (2 righe):**  
Gestisce la risposta ricevuta dal server e stampa l’indirizzo.  
Completa il ciclo richiesta-risposta nel sistema.

---

#### `props()`
```java
static Props props() {
    return Props.create(AddressBookClientActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per la creazione del client Akka.  
Permette un’istanza tipizzata da `ActorSystem`.

---

### Messaggi — AddressBook

#### `PutMsg`
```java
public class PutMsg {
    private final String name;
    private final String address;

    public PutMsg(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getName() { return name; }
    public String getAddress() { return address; }
}
```
**Descrizione (2 righe):**  
Messaggio per inserire un contatto nella rubrica.  
Contiene nome e indirizzo da registrare.

---

#### `GetMsg`
```java
public class GetMsg {
    private final String name;

    public GetMsg(String name) {
        this.name = name;
    }

    public String getName() { return name; }
}
```
**Descrizione (2 righe):**  
Messaggio per richiedere un indirizzo tramite nome.  
Usato dal client per ottenere i dati dal server.

---

#### `ReplyMsg`
```java
public class ReplyMsg {
    private final String address;

    public ReplyMsg(String address) {
        this.address = address;
    }

    public String getAddress() { return address; }
}
```
**Descrizione (2 righe):**  
Messaggio di risposta inviato dal server al client con l’indirizzo trovato.  
Chiude il ciclo di comunicazione della richiesta.

---

#### `ConfigMsg` (AddressBook)
```java
public class ConfigMsg {
    private final ActorRef server;

    public ConfigMsg(ActorRef server) {
        this.server = server;
    }

    public ActorRef getServerRef() { return server; }
}
```
**Descrizione (2 righe):**  
Messaggio di configurazione per fornire al client il riferimento al server.  
Permette la connessione dinamica degli attori nel sistema Akka.

---

## SLEEPY ECHO — Esercizio 4

### SleepyEchoServerActor.java

#### `SleepyEchoServerActor (costruttore)`
```java
public SleepyEchoServerActor() {
    awake = true;
}
```
**Descrizione (2 righe):**  
Inizializza l’attore in stato “awake”.  
Serve per impostare il comportamento iniziale prima della gestione di messaggi.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return awakeBehavior();
}
```
**Descrizione (2 righe):**  
Definisce il comportamento iniziale di ricezione messaggi.  
L’attore parte nel comportamento “awake”.

---

#### `awakeBehavior()`
```java
private Receive awakeBehavior() {
    return receiveBuilder()
            .match(SleepMsg.class, this::onSleep)
            .match(TextMsg.class, this::onText)
            .build();
}
```
**Descrizione (2 righe):**  
Comportamento attivo: gestisce messaggi di testo e comandi di sleep.  
Serve come stato principale di funzionamento dell’attore.

---

#### `sleepBehavior()`
```java
private Receive sleepBehavior() {
    return receiveBuilder()
            .match(WakeupMsg.class, this::onWakeup)
            .matchAny(msg -> stash())
            .build();
}
```
**Descrizione (2 righe):**  
Comportamento di sleep: accumula i messaggi con `stash()` finché non arriva `WakeupMsg`.  
Pattern utile per la sospensione temporanea dell’elaborazione.

---

#### `onSleep(SleepMsg msg)`
```java
private void onSleep(SleepMsg msg) {
    System.out.println("Server going to sleep");
    getContext().become(sleepBehavior());
    awake = false;
}
```
**Descrizione (2 righe):**  
Passa lo stato dell’attore a “sleep” cambiando comportamento con `become()`.  
Simula la sospensione delle operazioni con stampa di debug.

---

#### `onWakeup(WakeupMsg msg)`
```java
private void onWakeup(WakeupMsg msg) {
    System.out.println("Server waking up");
    getContext().become(awakeBehavior());
    unstashAll();
    awake = true;
}
```
**Descrizione (2 righe):**  
Riporta l’attore allo stato attivo sbloccando i messaggi in coda.  
Usa `unstashAll()` per processare tutto ciò che era stato accantonato.

---

#### `onText(TextMsg msg)`
```java
private void onText(TextMsg msg) {
    getSender().tell(new Msg("Echo: " + msg.getText()), getSelf());
}
```
**Descrizione (2 righe):**  
Risponde con un messaggio “Echo” al mittente.  
È il comportamento di base del server quando è attivo.

---

#### `props()`
```java
static Props props() {
    return Props.create(SleepyEchoServerActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per creare l’attore server.  
Mantiene la creazione pulita e riutilizzabile.

---

### EchoClientActor.java

#### `EchoClientActor (costruttore)`
```java
public EchoClientActor() {}
```
**Descrizione (2 righe):**  
Costruttore vuoto per creare un attore client pronto a ricevere configurazione.  
Utilizzato per collegarsi dinamicamente al server.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(ConfigMsg.class, this::configure)
            .match(TextMsg.class, this::onText)
            .match(SleepMsg.class, this::onSleep)
            .match(WakeupMsg.class, this::onWakeup)
            .match(Msg.class, this::onMsg)
            .build();
}
```
**Descrizione (2 righe):**  
Definisce la gestione dei messaggi dal server e i comandi sleep/wakeup.  
Integra configurazione, invio di testo e ricezione echo.

---

#### `configure(ConfigMsg msg)`
```java
private void configure(ConfigMsg msg) {
    server = msg.getServerRef();
}
```
**Descrizione (2 righe):**  
Imposta il riferimento all’attore server.  
Permette la comunicazione diretta tra client e server.

---

#### `onText(TextMsg msg)`
```java
private void onText(TextMsg msg) {
    server.tell(msg, getSelf());
}
```
**Descrizione (2 righe):**  
Inoltra il messaggio di testo al server per ottenere una risposta echo.  
Gestisce l’invio client→server.

---

#### `onSleep(SleepMsg msg)`
```java
private void onSleep(SleepMsg msg) {
    server.tell(msg, getSelf());
}
```
**Descrizione (2 righe):**  
Manda un comando di sleep al server.  
Usato per simulare la sospensione del servizio echo.

---

#### `onWakeup(WakeupMsg msg)`
```java
private void onWakeup(WakeupMsg msg) {
    server.tell(msg, getSelf());
}
```
**Descrizione (2 righe):**  
Invia un comando di riattivazione al server.  
Permette di testare la ripresa dell’elaborazione.

---

#### `onMsg(Msg msg)`
```java
private void onMsg(Msg msg) {
    System.out.println("Received echo: " + msg.getContent());
}
```
**Descrizione (2 righe):**  
Gestisce il messaggio di risposta echo proveniente dal server.  
Stampa il contenuto ricevuto come conferma della comunicazione.

---

#### `props()`
```java
static Props props() {
    return Props.create(EchoClientActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per creare l’attore client echo.  
Usata per istanziazioni tipizzate in `ActorSystem`.

---

### Messaggi — SleepyEcho

#### `TextMsg`
```java
public class TextMsg {
    private final String text;
    public TextMsg(String text) { this.text = text; }
    public String getText() { return text; }
}
```
**Descrizione (2 righe):**  
Messaggio contenente il testo da inviare al server echo.  
Usato come payload principale nel sistema.

---

#### `Msg`
```java
public class Msg {
    private final String content;
    public Msg(String content) { this.content = content; }
    public String getContent() { return content; }
}
```
**Descrizione (2 righe):**  
Messaggio di risposta echo dal server.  
Contiene il testo elaborato e restituito al client.

---

#### `SleepMsg`
```java
public class SleepMsg {}
```
**Descrizione (2 righe):**  
Messaggio comando per far passare il server in stato di sleep.  
Non contiene campi, usato solo come trigger.

---

#### `WakeupMsg`
```java
public class WakeupMsg {}
```
**Descrizione (2 righe):**  
Messaggio comando per risvegliare il server.  
Utilizzato per testare la funzionalità di `unstashAll()`.

---

#### `ConfigMsg` (SleepyEcho)
```java
public class ConfigMsg {
    private final ActorRef server;
    public ConfigMsg(ActorRef server) { this.server = server; }
    public ActorRef getServerRef() { return server; }
}
```
**Descrizione (2 righe):**  
Messaggio per configurare client o altri attori con il riferimento al server.  
Fornisce la connessione necessaria per la comunicazione diretta.

---

> Se manca qualcosa o vuoi aggiungere i prossimi esercizi, inviami i file e aggiorno questo documento (senza sovrascrivere le parti esistenti).



---

## SUPERVISED ADDRESS BOOK — Esercizio 5

### AddressBookSupervisorActor.java

#### `AddressBookSupervisorActor (costruttore)`
```java
public AddressBookSupervisorActor() {
    // inizializzazione e (eventuale) creazione figli/server
}
```
**Descrizione (2 righe):**  
Costruttore del supervisore: prepara wiring e stato iniziale.  
Punto di ingresso per collegare il server child e impostare la supervisione.

---

#### `createReceive()`
```java
@Override
public Receive createReceive() {
    return receiveBuilder()
            .match(PutMsg.class, this::onPut)
            .match(GetMsg.class, this::onGet)
            .build();
}
```
**Descrizione (2 righe):**  
Definisce il comportamento del supervisore per inoltrare/gestire Put e Get.  
Punto centrale per coordinare messaggi e figli.

---

#### `supervisorStrategy()`
```java
@Override
public SupervisorStrategy supervisorStrategy() {
    return strategy;
}
```
**Descrizione (2 righe):**  
Strategia di supervisione: decide come reagire ai fallimenti dei figli (resume/restart/stop).  
Fondamentale per garantire che lo stato valido della rubrica non vada perso durante i fault.

---

#### `onPut(PutMsg msg)`
```java
private void onPut(PutMsg msg) {
    // inoltro o gestione diretta del Put
}
```
**Descrizione (2 righe):**  
Gestisce inserimenti/aggiornamenti e può innescare failure su input invalidi (es. chiave "Fail!").  
Rilevante per testare la resilienza e la supervisione.

---

#### `onGet(GetMsg msg)`
```java
private void onGet(GetMsg msg) {
    // inoltro o gestione diretta del Get
}
```
**Descrizione (2 righe):**  
Recupera i dati e risponde al chiamante.  
Completa la parte di lettura del ciclo richiesta/risposta.

---

#### `props()`
```java
static Props props() {
    return Props.create(AddressBookSupervisorActor.class);
}
```
**Descrizione (2 righe):**  
Factory standard per creare il supervisore.  
Migliora pulizia e riusabilità della creazione da `ActorSystem` o altri attori.

---

### (Riferimenti condivisi) AddressBookServerActor.java — metodi già riportati sopra

> Il server riusa `createReceive`, `onPut`, `onGet`, `props` come in Esercizio 3; in questo esercizio opera **sotto supervisione**, così da preservare lo stato o ripartire in modo controllato a fronte di fault.

### Messaggi — Esercizi 3 & 5 (riuso)

> `PutMsg`, `GetMsg`, `ReplyMsg`, `ConfigMsg` sono gli stessi già elencati nella sezione AddressBook. In Esercizio 5 vengono usati per verificare il comportamento di **supervisione e failover**.

