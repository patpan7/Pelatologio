package org.easytech.pelatologio;
import javax.sip.*;
import javax.sip.address.*;
import javax.sip.message.*;
import javax.sip.header.*;
import java.util.Properties;

public class SIPClient implements SipListener {
    private SipFactory sipFactory;
    private SipStack sipStack;
    private SipProvider sipProvider;
    private String username;
    private String password;
    private String domain;
    private int port;
    private String transport;

    public SIPClient(String username, String password, String domain, int port, String transport) {
        this.username = username;
        this.password = password;
        this.domain = domain;
        this.port = port;
        this.transport = transport;
    }

    public void init() throws Exception {
        // Ρυθμίσεις SIP
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "SIPClientStack");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sipdebug.txt");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "sipserverlog.txt");
        properties.setProperty("gov.nist.javax.sip.READ_TIMEOUT", "1000");
        properties.setProperty("gov.nist.javax.sip.CACHE_CLIENT_CONNECTIONS", "false");


        // Δημιουργία SipStack
        sipFactory = SipFactory.getInstance();
        sipFactory.setPathName("gov.nist");
        sipStack = sipFactory.createSipStack(properties);
        ListeningPoint listeningPoint = sipStack.createListeningPoint("0.0.0.0", port, transport);

        // Δημιουργία SipProvider
        sipProvider = sipStack.createSipProvider(listeningPoint);
        sipProvider.addSipListener(this);

        // Εγγραφή στο τηλεφωνικό κέντρο
        register();
    }

    private void register() throws Exception {
        // Δημιουργία εργοστασίων για μηνύματα και headers
        AddressFactory addressFactory = sipFactory.createAddressFactory();
        HeaderFactory headerFactory = sipFactory.createHeaderFactory();
        MessageFactory messageFactory = sipFactory.createMessageFactory();

        // Δημιουργία "From" και "To" Headers
        Address address = addressFactory.createAddress("sip:" + username + "@" + domain);
        FromHeader fromHeader = headerFactory.createFromHeader(address, "12345");
        ToHeader toHeader = headerFactory.createToHeader(address, null);

        // Δημιουργία Contact Header
        SipURI contactURI = addressFactory.createSipURI(username, "0.0.0.0:" + port);
        Address contactAddress = addressFactory.createAddress(contactURI);
        ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

        // Δημιουργία Via Header
        ViaHeader viaHeader = headerFactory.createViaHeader("0.0.0.0", port, transport, null);

        // Δημιουργία Max-Forwards Header
        MaxForwardsHeader maxForwardsHeader = headerFactory.createMaxForwardsHeader(70);

        // Δημιουργία CSeq Header
        CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER);

        // Δημιουργία Call-ID Header
        CallIdHeader callIdHeader = sipProvider.getNewCallId();

        // Δημιουργία Request
        Request request = messageFactory.createRequest(
                "REGISTER sip:" + domain + " SIP/2.0"
        );
        request.addHeader(fromHeader);
        request.addHeader(toHeader);
        request.addHeader(contactHeader);
        request.addHeader(viaHeader);
        request.addHeader(maxForwardsHeader);
        request.addHeader(cSeqHeader);
        request.addHeader(callIdHeader);

        // Αποστολή Request
        ClientTransaction transaction = sipProvider.getNewClientTransaction(request);
        transaction.sendRequest();
        System.out.println("REGISTER request sent.");
    }


    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        if (request.getMethod().equals(Request.INVITE)) {
            // Ειδοποίηση για εισερχόμενη κλήση
            System.out.println("Incoming call from: " + request.getHeader("From"));
            showPopup(request.getHeader("From").toString());
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        int statusCode = response.getStatusCode();

        if (statusCode == Response.OK) {
            // Επιτυχής εγγραφή
            System.out.println("SIP Registration Successful!");
        } else if (statusCode >= 400) {
            // Αποτυχία εγγραφής
            System.out.println("SIP Registration Failed! Status Code: " + statusCode);
        }
    }
    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        System.err.println("SIP Timeout: " + timeoutEvent.getTimeout());
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.err.println("IOException: " + exceptionEvent.getHost());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        System.out.println("Transaction Terminated");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        System.out.println("Dialog Terminated");
    }

    private void showPopup(String callingNumber) {
        // Εμφάνιση popup για την κλήση
        javax.swing.SwingUtilities.invokeLater(() -> {
            javax.swing.JOptionPane.showMessageDialog(null, "Incoming call from: " + callingNumber);
        });
    }

}
