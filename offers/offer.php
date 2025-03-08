<?php
include 'db.php';

if (!isset($_GET['id'])) {
    die("Λάθος: Δεν υπάρχει προσφορά");
}

$offerId = $_GET['id'];

$sql = "SELECT s.id, s.offerDate, s.description, s.hours, s.status, 
               s.customerId, s.response_date, 
               c.name, c.mobile, c.email, c.address 
        FROM Offers s 
        LEFT JOIN Customers c ON s.customerId = c.code 
        WHERE s.id = ?";

$params = array($offerId);
$stmt = sqlsrv_query($conn, $sql, $params);

if ($row = sqlsrv_fetch_array($stmt, SQLSRV_FETCH_ASSOC)) {
    $offerDate = $row['offerDate']->format('Y-m-d');
    $description = $row['description'];
    $hours = $row['hours'];
    $status = trim($row['status']); // Λήψη της κατάστασης
    $responseDate = $row['response_date'] ? $row['response_date']->format('Y-m-d') : "Σε αναμονή";

    // Στοιχεία πελάτη
    $customerName = $row['name'];
    $customerPhone = $row['mobile'];
    $customerEmail = $row['email'];
    $customerAddress = $row['address'];
} else {
    die("Η προσφορά δεν βρέθηκε");
}
?>

<!DOCTYPE html>
<html lang="el">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Προσφορά #<?php echo $offerId; ?></title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; }
        .container { max-width: 600px; margin: auto; padding: 20px; border: 1px solid #ddd; border-radius: 10px; }
        h2 { text-align: center; }
        .info { margin-bottom: 10px; }
        .buttons { text-align: center; margin-top: 20px; }
        button { padding: 10px 20px; font-size: 16px; margin: 5px; cursor: pointer; }
        button:disabled { background-color: #ccc; cursor: not-allowed; }
    </style>
</head>
<body>
    <div class="container">
        <h2>Προσφορά #<?php echo $offerId; ?></h2>
        <div class="info"><strong>Ημερομηνία:</strong> <?php echo $offerDate; ?></div>
        <div class="info"><strong>Περιγραφή:</strong> <?php echo $description; ?></div>
        <div class="info"><strong>Χρεώσιμες Ώρες:</strong> <?php echo $hours; ?></div>
        <div class="info"><strong>Κατάσταση:</strong> <?php echo $status; ?></div>
        <div class="info"><strong>Απάντηση Πελάτη:</strong> <?php echo $responseDate; ?></div>

        <h3>Στοιχεία Πελάτη</h3>
        <div class="info"><strong>Όνομα:</strong> <?php echo $customerName; ?></div>
        <div class="info"><strong>Τηλέφωνο:</strong> <?php echo $customerPhone; ?></div>
        <div class="info"><strong>Email:</strong> <?php echo $customerEmail; ?></div>
        <div class="info"><strong>Διεύθυνση:</strong> <?php echo $customerAddress; ?></div>

        <div class="buttons">
            <!-- Εμφάνιση κουμπιών μόνο αν η κατάσταση είναι "Αναμονή" -->
            <?php if ($status == 'Αναμονή'): ?>
                <form action="update_offer.php" method="post">
                    <input type="hidden" name="offerId" value="<?php echo $offerId; ?>">
                    <button type="submit" name="action" value="accept" style="background-color: green; color: white;">Αποδοχή</button>
                    <button type="submit" name="action" value="reject" style="background-color: red; color: white;">Απόρριψη</button>
                </form>
            <?php else: ?>
                <button disabled style="background-color: #ccc; cursor: not-allowed;">Η προσφορά δεν μπορεί να τροποποιηθεί</button>
            <?php endif; ?>
        </div>
		
        <!-- Τιμοκατάλογος Υπηρεσιών -->
        <div class="pricing">
            <h3>ΒΑΣΙΚΟΣ ΤΙΜΟΚΑΤΑΛΟΓΟΣ ΥΠΗΡΕΣΙΩΝ</h3>
            <ul>
                <strong>Τηλεφωνική υποστήριξη:</strong> 
				<li>Επιπλέων τηλεφωνική υποστήριξη με χρέωση 50€/ώρα.</li>
				<li>Ελάχιστη χρέωση 30 λεπτά.</li>
                <strong>Online υποστήριξη:</strong> 
				<li>Χρέωση 50€/ώρα.</li>
				<li>Ελάχιστη χρέωση 30 λεπτά.</li>
				<li>Μετέπειτα χρέωση ανά 30 λεπτά.</li>
                <strong>Επίσκεψη τεχνικού:</strong>
				<li>Χρέωση 65€/ώρα.</li>
				<li>Ελάχιστη χρέωση 1 ώρα.</li>
				<li>Μετέπειτα χρέωση ανά 30 λεπτά.</li>
				<li>Η χρεώσιμη ώρα νοείται από την ώρα εκκίνησης του τεχνικού από την έδρα μας και επιστροφή του στην έδρα μας.</li>
                <strong>Αλλαγές Τιμοκαταλόγου:</strong>
				<li>Κατόπιν ραντεβού σε εργάσιμες μέρες και ώρες.</li>
				<li>Χρέωση 50€/ώρα για online ραντεβού ή 65€/ώρα για επίσκεψη τεχνικού (ισχύουν οι όροι επίσκεψης τεχνικού).</li>
				<li>Ελάχιστη χρέωση 1 ώρα.</li>
				<li>Μετέπειτα χρέωση ανά 30 λεπτά.</li>
                <strong>Χρεώσεις υπηρεσιών εκτός ωραρίου:</strong>
				<li>Καθημερινά από τις 17:00 έως 00:00</li>
				<li>Σάββατο από τις 14:00 έως 00:00</li>
				<li>Κυριακή από τις 10:00 έως 00:00</li>
				<li>Οι υφιστάμενες χρεώσεις ώρας παραμένουν ίδιες.</li>
				<li>Ελάχιστη χρέωση 1 ώρα για Online υποστήριξη ή Επίσκεψη τεχνικού.</li>
				<li>Μετέπειτα χρέωση ανά 1 ώρα.</li>
            </ul>
        </div>
    </div>
</body>
</html>
    </div>
</body>
</html>
