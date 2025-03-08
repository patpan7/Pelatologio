<?php
include 'db.php';

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $offerId = $_POST['offerId'];
    $action = $_POST['action'];

    if ($action == 'accept') {
        $status = "Αποδοχή";
    } elseif ($action == 'reject') {
        $status = "Απορρίψη";
    } else {
        die("Μη έγκυρη ενέργεια");
    }

$sql = "UPDATE Offers 
        SET status = ?, response_date = GETDATE(), last_updated = GETDATE() 
        WHERE id = ? AND status <> ?"; 

$params = array($status, $offerId, $status);
$stmt = sqlsrv_query($conn, $sql, $params);

    if ($stmt) {
        echo "Η προσφορά ενημερώθηκε.";
    } else {
        echo "Σφάλμα κατά την ενημέρωση.";
    }
}
?>
