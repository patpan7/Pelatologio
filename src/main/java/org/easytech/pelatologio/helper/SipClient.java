package org.easytech.pelatologio.helper;

import org.easytech.pelatologio.AppSettings;

import javax.sip.*;
import javax.sip.address.Address;
import javax.sip.address.AddressFactory;
import javax.sip.address.SipURI;
import javax.sip.header.*;
import javax.sip.message.MessageFactory;
import javax.sip.message.Request;
import javax.sip.message.Response;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.TooManyListenersException;
import java.util.function.Consumer;
import org.easytech.pelatologio.helper.ActiveCallState;

public class SipClient implements SipListener {

    private SipStack sipStack;
    private SipProvider sipProvider;
    private MessageFactory messageFactory;
    private HeaderFactory headerFactory;
    private AddressFactory addressFactory;
    private SipFactory sipFactory;

    private String sipUser;
    private String sipPassword;
    private String sipDomain;
    private int sipPort;
    private String sipTransport;

    private ListeningPoint listeningPoint;
    private Consumer<String> onIncomingCallCallback;

    public SipClient(Consumer<String> onIncomingCallCallback) {
        this.onIncomingCallCallback = onIncomingCallCallback;
        loadSipSettings();
    }

    private void loadSipSettings() {
        sipUser = AppSettings.loadSetting("sipUser");
        sipPassword = AppSettings.loadSetting("sipPassword");
        sipDomain = AppSettings.loadSetting("sipDomain");
        sipPort = Integer.parseInt(AppSettings.loadSetting("sipPort"));
        sipTransport = AppSettings.loadSetting("sipTransport");
    }

    public void start() throws PeerUnavailableException, TransportNotSupportedException, InvalidArgumentException, TooManyListenersException, ObjectInUseException {
        sipFactory = SipFactory.getInstance();
        //sipFactory.setSipStackClassName(SipStackImpl.class.getName());
        Properties properties = new Properties();
        properties.setProperty("javax.sip.STACK_NAME", "PelatologioSipClient");
        properties.setProperty("javax.sip.IP_ADDRESS", AppSettings.loadSetting("localIpAddress")); // Local IP of the machine
        properties.setProperty("javax.sip.OUTBOUND_PROXY", sipDomain + ":" + sipPort + "/" + sipTransport);
        properties.setProperty("javax.sip.ROUTER_PATH", "gov.nist.javax.sip.stack.DefaultRouter");
        properties.setProperty("javax.sip.STACK_NAME", "PelatologioSipClient");
        properties.setProperty("javax.sip.RETRANSMISSION_FILTER", "true");
        properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "ERROR");
        properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", "sip_debug.log");
        properties.setProperty("gov.nist.javax.sip.SERVER_LOG", "sip_server.log");

        sipStack = sipFactory.createSipStack(properties);
        headerFactory = sipFactory.createHeaderFactory();
        addressFactory = sipFactory.createAddressFactory();
        messageFactory = sipFactory.createMessageFactory();

        listeningPoint = sipStack.createListeningPoint(sipPort, sipTransport);
        sipProvider = sipStack.createSipProvider(listeningPoint);
        sipProvider.addSipListener(this);

        try {
            register();
        } catch (ParseException | InvalidArgumentException e) {
            System.err.println("Error during SIP registration: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("SIP Client started on port " + sipPort + " with transport " + sipTransport);
    }

    private void register() throws ParseException, InvalidArgumentException {
        try {
            // From Address
            Address fromAddress = addressFactory.createAddress("sip:" + sipUser + "@" + sipDomain);
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345"); // Tag is random

            // To Address
            Address toAddress = addressFactory.createAddress("sip:" + sipUser + "@" + sipDomain);
            ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

            // Request URI
            SipURI requestURI = addressFactory.createSipURI(sipUser, sipDomain);

            // Call-ID
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // CSeq
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.REGISTER);

            // Via Header
            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = headerFactory.createViaHeader(AppSettings.loadSetting("localIpAddress"), sipPort, sipTransport, null);
            viaHeaders.add(viaHeader);

            // Max-Forwards
            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

            // Contact
            SipURI contactURI = addressFactory.createSipURI(sipUser, AppSettings.loadSetting("localIpAddress"));
            contactURI.setPort(listeningPoint.getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

            // Create the REGISTER request
            Request registerRequest = messageFactory.createRequest(
                    requestURI, Request.REGISTER, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
            registerRequest.addHeader(contactHeader);

            // Send the request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(registerRequest);
            clientTransaction.sendRequest();

        } catch (Exception e) {
            System.err.println("Error sending REGISTER: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void makeCall(String targetPhoneNumber) throws ParseException, InvalidArgumentException {
        try {
            // From Address
            Address fromAddress = addressFactory.createAddress("sip:" + sipUser + "@" + sipDomain);
            FromHeader fromHeader = headerFactory.createFromHeader(fromAddress, "12345");

            // To Address
            Address toAddress = addressFactory.createAddress("sip:" + targetPhoneNumber + "@" + sipDomain);
            ToHeader toHeader = headerFactory.createToHeader(toAddress, null);

            // Request URI
            SipURI requestURI = addressFactory.createSipURI(targetPhoneNumber, sipDomain);

            // Call-ID
            CallIdHeader callIdHeader = sipProvider.getNewCallId();

            // CSeq
            CSeqHeader cSeqHeader = headerFactory.createCSeqHeader(1L, Request.INVITE);

            // Via Header
            ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
            ViaHeader viaHeader = headerFactory.createViaHeader(AppSettings.loadSetting("localIpAddress"), sipPort, sipTransport, null);
            viaHeaders.add(viaHeader);

            // Max-Forwards
            MaxForwardsHeader maxForwards = headerFactory.createMaxForwardsHeader(70);

            // Contact
            SipURI contactURI = addressFactory.createSipURI(sipUser, AppSettings.loadSetting("localIpAddress"));
            contactURI.setPort(listeningPoint.getPort());
            Address contactAddress = addressFactory.createAddress(contactURI);
            ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);

            // Content-Type (SDP for audio)
            ContentTypeHeader contentTypeHeader = headerFactory.createContentTypeHeader("application", "sdp");

            // Create the INVITE request
            Request inviteRequest = messageFactory.createRequest(
                    requestURI, Request.INVITE, callIdHeader, cSeqHeader, fromHeader, toHeader, viaHeaders, maxForwards);
            inviteRequest.addHeader(contactHeader);
            inviteRequest.setContent("v=0\r\no=- 2 0 IN IP4 " + AppSettings.loadSetting("localIpAddress") + "\r\ns=-\r\nc=IN IP4 " + AppSettings.loadSetting("localIpAddress") + "\r\nt=0 0\r\nm=audio " + (sipPort + 2) + " RTP/AVP 0\r\na=rtpmap:0 PCMU/8000\r\n", contentTypeHeader);

            // Send the request
            ClientTransaction clientTransaction = sipProvider.getNewClientTransaction(inviteRequest);
            clientTransaction.sendRequest();

        } catch (Exception e) {
            System.err.println("Error sending INVITE: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void processRequest(RequestEvent requestEvent) {
        Request request = requestEvent.getRequest();
        ServerTransaction serverTransaction = requestEvent.getRequest().getMethod().equals(Request.INVITE) ? requestEvent.getServerTransaction() : null;

        if (request.getMethod().equals(Request.INVITE)) {
            try {
                // Try to get the caller ID from the From header
                FromHeader fromHeader = (FromHeader) request.getHeader(FromHeader.NAME);
                String callerId = fromHeader.getAddress().getURI().toString();
                if (callerId.startsWith("sip:")) {
                    callerId = callerId.substring(4);
                }
                if (callerId.contains("@")) {
                    callerId = callerId.substring(0, callerId.indexOf("@"));
                }

                // Send a TRYING response immediately
                Response tryingResponse = messageFactory.createResponse(Response.TRYING, request);
                if (serverTransaction == null) {
                    serverTransaction = sipProvider.getNewServerTransaction(request);
                }
                serverTransaction.sendResponse(tryingResponse);

                // Send a RINGING response
                Response ringingResponse = messageFactory.createResponse(Response.RINGING, request);
                ringingResponse.addHeader(headerFactory.createContactHeader(addressFactory.createAddress(addressFactory.createSipURI(null, listeningPoint.getIPAddress()))));
                serverTransaction.sendResponse(ringingResponse);

                // Notify the main application
                if (onIncomingCallCallback != null) {
                    // Normalize callerId before sending to callback
                    if (callerId != null) {
                        callerId = callerId.replace("+", "").replace("-", "").replace(" ", "");
                        if (callerId.startsWith("30")) {
                            callerId = callerId.substring(2);
                        }
                    }
                    ActiveCallState.setPendingCall(callerId);
                    onIncomingCallCallback.accept(callerId);
                }

            } catch (Exception e) {
                System.err.println("Error processing INVITE request: " + e.getMessage());
                e.printStackTrace();
            }
        } else if (request.getMethod().equals(Request.ACK)) {
            // Handle ACK (acknowledgment of INVITE response)
            // System.out.println("Received ACK.");
        } else if (request.getMethod().equals(Request.BYE)) {
            // Handle BYE (call termination)
            try {
                Response okResponse = messageFactory.createResponse(Response.OK, request);
                if (serverTransaction == null) {
                    serverTransaction = sipProvider.getNewServerTransaction(request);
                }
                serverTransaction.sendResponse(okResponse);
                ActiveCallState.clearPendingCall();
                // System.out.println("Received BYE, sent OK.");
            } catch (Exception e) {
                System.err.println("Error processing BYE: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    @Override
    public void processResponse(ResponseEvent responseEvent) {
        Response response = responseEvent.getResponse();
        ClientTransaction clientTransaction = responseEvent.getClientTransaction();

        int statusCode = response.getStatusCode();
        Request originalRequest = clientTransaction.getRequest();

        CSeqHeader cseq = (CSeqHeader) response.getHeader(CSeqHeader.NAME);

        if (cseq.getMethod().equals(Request.REGISTER)) {
            if (response.getStatusCode() == Response.OK) {
                // System.out.println("Successfully registered with SIP server.");
            } else if (statusCode == Response.UNAUTHORIZED || statusCode == Response.PROXY_AUTHENTICATION_REQUIRED) {
                try {
                    WWWAuthenticateHeader authHeader = (WWWAuthenticateHeader) response.getHeader(WWWAuthenticateHeader.NAME);

                    String realm = authHeader.getRealm();
                    String nonce = authHeader.getNonce();
                    String uri = "sip:" + sipDomain;
                    String username = sipUser;
                    String password = sipPassword;
                    String method = Request.REGISTER;

                    // Digest = MD5( MD5(username:realm:password):nonce:MD5(method:uri) )
                    String ha1 = md5(username + ":" + realm + ":" + password);
                    String ha2 = md5(method + ":" + uri);
                    String responseDigest = md5(ha1 + ":" + nonce + ":" + ha2);
                    ArrayList<ViaHeader> viaHeaders = new ArrayList<>();
                    ViaHeader viaHeader = headerFactory.createViaHeader(
                            AppSettings.loadSetting("localIpAddress"),
                            sipPort,
                            sipTransport,
                            null
                    );
                    viaHeaders.add(viaHeader);
                    // Recreate REGISTER with Authorization header
                    Request newRegister = messageFactory.createRequest(originalRequest.getRequestURI(), Request.REGISTER,
                            sipProvider.getNewCallId(),
                            headerFactory.createCSeqHeader(2L, Request.REGISTER),
                            (FromHeader) originalRequest.getHeader(FromHeader.NAME),
                            (ToHeader) originalRequest.getHeader(ToHeader.NAME),
                            viaHeaders,
                            headerFactory.createMaxForwardsHeader(70)
                    );

                    SipURI contactURI = addressFactory.createSipURI(sipUser, AppSettings.loadSetting("localIpAddress"));
                    contactURI.setPort(listeningPoint.getPort());
                    Address contactAddress = addressFactory.createAddress(contactURI);
                    ContactHeader contactHeader = headerFactory.createContactHeader(contactAddress);
                    newRegister.addHeader(contactHeader);

                    AuthorizationHeader authorizationHeader = headerFactory.createAuthorizationHeader("Digest");
                    authorizationHeader.setUsername(username);
                    authorizationHeader.setRealm(realm);
                    authorizationHeader.setNonce(nonce);
                    authorizationHeader.setURI(addressFactory.createURI(uri));
                    authorizationHeader.setResponse(responseDigest);
                    authorizationHeader.setAlgorithm("MD5");

                    newRegister.addHeader(authorizationHeader);

                    ClientTransaction retryTransaction = sipProvider.getNewClientTransaction(newRegister);
                    retryTransaction.sendRequest();

                } catch (Exception authEx) {
                    System.err.println("Failed to retry REGISTER with authentication: " + authEx.getMessage());
                    authEx.printStackTrace();
                }
            } else {
                System.err.println("Registration failed with status: " + response.getStatusCode());
            }
        } else if (cseq.getMethod().equals(Request.INVITE)) {
            if (response.getStatusCode() == Response.OK) {
                // Call established, send ACK
                try {
                    Dialog dialog = clientTransaction.getDialog();
                    Request ackRequest = dialog.createAck(cseq.getSeqNumber());
                    dialog.sendAck(ackRequest);
                    // System.out.println("Call established, sent ACK.\n" + ackRequest);
                } catch (Exception e) {
                    System.err.println("Error sending ACK: " + e.getMessage());
                    e.printStackTrace();
                }
            } else if (response.getStatusCode() >= 300) {
                System.err.println("Call failed with status: " + response.getStatusCode());
            }
        }
    }

    @Override
    public void processTimeout(TimeoutEvent timeoutEvent) {
        System.err.println("SIP Timeout: " + timeoutEvent.getTimeout());
    }

    @Override
    public void processIOException(IOExceptionEvent exceptionEvent) {
        System.err.println("SIP IOException: " + exceptionEvent.getHost() + ":" + exceptionEvent.getPort() + " - " + exceptionEvent.toString());
    }

    @Override
    public void processTransactionTerminated(TransactionTerminatedEvent transactionTerminatedEvent) {
        // System.out.println("SIP Transaction Terminated.");
    }

    @Override
    public void processDialogTerminated(DialogTerminatedEvent dialogTerminatedEvent) {
        // System.out.println("SIP Dialog Terminated.");
    }

    public void stop() throws ObjectInUseException {
        if (sipProvider != null) {
            sipProvider.removeSipListener(this);
            sipStack.deleteSipProvider(sipProvider);
            sipStack.stop();
            sipStack = null;
        }
    }

    private String md5(String data) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(data.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate MD5", e);
        }
    }

    /**
     * Simulates an incoming call for debugging purposes.
     *
     * @param callerId The phone number of the simulated caller.
     */
    public void simulateIncomingCall(String callerId) {
        System.out.println("--- SIMULATING INCOMING CALL from " + callerId + " ---");
        if (onIncomingCallCallback != null) {
            // Normalize callerId before sending to callback, same as in processRequest
            String normalizedCallerId = callerId.replace("+", "").replace("-", "").replace(" ", "");
            if (normalizedCallerId.startsWith("30")) {
                normalizedCallerId = normalizedCallerId.substring(2);
            }
            ActiveCallState.setPendingCall(normalizedCallerId);
            onIncomingCallCallback.accept(normalizedCallerId);
        }
    }
}