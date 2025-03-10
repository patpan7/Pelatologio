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
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Προσφορά #<?php echo $offerId; ?></title>

    <title>Προσφορά</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="style.css">
</head>

<body>
    <div class="container mt-4">
        <h2>Προσφορά #<?php echo $offerId; ?></h2>

        <!-- Κάρτα Προσφοράς -->
        <div class="card">
            <div class="card-header">Στοιχεία Προσφοράς</div>
            <div class="card-body">
                <p><strong>Ημερομηνία:</strong> <?php echo $offerDate; ?></p>
                <p><strong>Περιγραφή:</strong> <?php echo $description; ?></p>
                <p><strong>Χρεώσιμες Ώρες:</strong> <?php echo $hours; ?></p>
                <p><strong>Κατάσταση:</strong>
                    <?php
                    if ($status == 'Απορρίψη') {
                        echo '<i class="fas fa-times-circle text-danger"></i> Απόρριψη';
                    } elseif ($status == 'Αποδοχή') {
                        echo '<i class="fas fa-check-circle text-success"></i> Αποδοχή';
                    } else {
                        echo '<i class="fas fa-hourglass-half text-warning"></i> Αναμονή';
                    }
                    ?>
                    </p>
                <p><strong>Απάντηση Πελάτη:</strong> <?php echo $responseDate; ?></p>
            </div>
        </div>
        <!-- Κάρτα Στοιχείων Πελάτη -->
        <div class="card">
            <div class="card-header">Στοιχεία Πελάτη</div>
            <div class="card-body">
                <p><strong>Όνομα:</strong> <?php echo $customerName; ?></p>
                <p><strong>Τηλέφωνο:</strong> <?php echo $customerPhone; ?></p>
                <p><strong>Email:</strong> <?php echo $customerEmail; ?></p>
                <p><strong>Διεύθυνση:</strong> <?php echo $customerAddress; ?></p>
            </div>
        </div>
        <div class="buttons">
            <!-- Εμφάνιση κουμπιών μόνο αν η κατάσταση είναι "Αναμονή" -->
            <?php if ($status == 'Αναμονή'): ?>
                <form action="update_offer.php" method="post">
                    <input type="hidden" name="offerId" value="<?php echo $offerId; ?>">
                    <button type="submit" name="action" value="accept" class="btn btn-success btn-sm">Αποδοχή</button>
                    <button type="submit" name="action" value="reject" class="btn btn-danger btn-sm">Απόρριψη</button>
                </form>
            <?php else: ?>
                <button disabled>Η προσφορά δεν μπορεί να τροποποιηθεί</button>
            <?php endif; ?>
        </div>

        <!-- Τιμοκατάλογος Υπηρεσιών -->
        <div class="pricing">
            <h3>ΒΑΣΙΚΟΣ ΤΙΜΟΚΑΤΑΛΟΓΟΣ ΥΠΗΡΕΣΙΩΝ</h3>
            <ul>
                <li><strong>Τηλεφωνική υποστήριξη:</strong> Επιπλέον τηλεφωνική υποστήριξη με χρέωση 50€/ώρα. Ελάχιστη
                    χρέωση 30 λεπτά.</li>
                <li><strong>Online υποστήριξη:</strong> Χρέωση 50€/ώρα. Ελάχιστη χρέωση 30 λεπτά. Μετέπειτα χρέωση ανά
                    30 λεπτά.</li>
                <li><strong>Επίσκεψη τεχνικού:</strong> Χρέωση 65€/ώρα. Ελάχιστη χρέωση 1 ώρα. Μετέπειτα χρέωση ανά 30
                    λεπτά.</li>
                <li><strong>Αλλαγές Τιμοκαταλόγου:</strong> Κατόπιν ραντεβού σε εργάσιμες μέρες και ώρες. Χρέωση 50€/ώρα
                    για online ραντεβού ή 65€/ώρα για επίσκεψη τεχνικού.</li>
                <li><strong>Χρεώσεις εκτός ωραρίου:</strong> Καθημερινά από τις 17:00 έως 00:00. Σάββατο από τις 14:00
                    έως 00:00. Κυριακή από τις 10:00 έως 00:00.</li>
            </ul>
        </div>
    </div>
</body>

</html>