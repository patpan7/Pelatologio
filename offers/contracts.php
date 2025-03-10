<?php session_start();
if (!isset($_SESSION['afm'])) {
    header("Location: index.php");
    exit;
}
include 'db.php';
$afm = $_SESSION['afm'];

// Διορθωμένο SQL Query με τα σωστά πεδία
$sqlContracts = "SELECT id, endDate, title, price FROM Subscriptions WHERE customerId = (SELECT code FROM Customers WHERE afm = ?);";
$paramsContracts = array($afm);
$stmtContracts = sqlsrv_query($conn, $sqlContracts, $paramsContracts);
?>
<!DOCTYPE html>
<html lang="el">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Συμβόλαια</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="style.css">
</head>

<body>
    <?php include 'navbar.php'; ?>
    <div class="container mt-5">
        <h2 class="text-center mb-4">Τα Συμβόλαια σας</h2>
        <div class="row">
            <?php while ($contract = sqlsrv_fetch_array($stmtContracts, SQLSRV_FETCH_ASSOC)): ?>
                <div class="col-md-4 d-flex mb-4">
                    <div class="card bank-card shadow-lg h-100 w-100">
                        <div class="card-header bg-primary text-white bank-header">
                            <h5 class="mb-0">Συμβόλαιο #<?php echo htmlspecialchars($contract['id']); ?>
                                <h5>
                        </div>
                        <div class="card-body">
                            <p><strong>Τίτλος:</strong> <?php echo htmlspecialchars($contract['title']); ?></p>
                            <p><strong>Ημερομηνία Λήξης:</strong> <?php echo $contract['endDate']->format('d-m-Y'); ?></p>
                            <p><strong>Τιμή:</strong> <?php echo htmlspecialchars($contract['price']); ?>€</p>
                        </div>
                    </div>
                </div>
            <?php endwhile; ?>
        </div>
    </div>
    <!-- Κουμπί επιστροφής -->
    <div class="text-center mt-4">
        <a href="dashboard.php" class="btn btn-secondary"><i class="fas fa-arrow-left"></i> Επιστροφή</a>
    </div>
    </div>
</body>

</html>