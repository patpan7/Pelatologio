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
}
?>

<!DOCTYPE html>
<html lang="el">
<head>
    <meta charset="UTF-8">
    <title>Προσφορά</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="style.css">
</head>
<body>
    <?php include 'navbar.php'; ?>
    <div class="container mt-5">
        <!-- Card Header -->
        <div class="card">
            <div class="card-header bg-primary text-white">
                <h5 class="mb-0">Προσφορά #<?php echo htmlspecialchars($offerId); ?></h5>
            </div>

            <!-- Card Body -->
            <div class="card-body">
                <p class="text-center">
                    <?php
                    if (isset($stmt) && $stmt) {
                        echo "<span class='text-success'>Η προσφορά ενημερώθηκε επιτυχώς.</span>";
                    } else {
                        echo "<span class='text-danger'>Σφάλμα κατά την ενημέρωση της προσφοράς.</span>";
                    }
                    ?>
                </p>
            </div>
        </div>

        <!-- Back Button -->
        <div class="text-center mt-4">
            <a href="index.php" class="btn btn-secondary"><i class="fas fa-arrow-left"></i> Επιστροφή στο Portal</a>
        </div>
    </div>
</body>
</html>
