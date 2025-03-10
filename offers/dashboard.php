<?php
session_start();
if (!isset($_SESSION['afm'])) {
    header("Location: index.php");
    exit;
}
include 'db.php';

$afm = $_SESSION['afm'];
$sql = "SELECT code, name FROM Customers WHERE afm = ?";
$params = array($afm);
$stmt = sqlsrv_query($conn, $sql, $params);
$customer = sqlsrv_fetch_array($stmt, SQLSRV_FETCH_ASSOC);
?>
<!DOCTYPE html>
<html lang="el">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Dashboard</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="style.css">
</head>

<body>
    <?php include 'navbar.php'; ?>
    <div class="container mt-4">
        <h2 class="text-center">Καλώς ήρθες, <?php echo $customer['name']; ?>!</h2>

        <div class="row mt-4">
            <div class="col-md-4 d-flex mb-4">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Οι Προσφορές σας</h5>
                        <p class="card-text">Δείτε τις προσφορές που σας έχουμε αποστείλει.</p>
                        <a href="offers.php" class="btn btn-primary">Προβολή Προσφορών</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4 d-flex mb-4">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Τα Συμβόλαιά σας</h5>
                        <p class="card-text">Εδώ μπορείτε να δείτε τα ενεργά συμβόλαιά σας.</p>
                        <a href="contracts.php" class="btn btn-primary">Προβολή Συμβολαίων</a>
                    </div>
                </div>
            </div>
            <div class="col-md-4 d-flex mb-4">
                <div class="card shadow-sm">
                    <div class="card-body">
                        <h5 class="card-title">Τραπεζικοί λογαριασμοί</h5>
                        <p class="card-text">Εδώ μπορείτε να δείτε τους τραπεχικούς μας λογαριασμούς.</p>
                        <a href="bank_accounts.php" class="btn btn-primary">Προβολή Λογαριασμών</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</body>

</html>