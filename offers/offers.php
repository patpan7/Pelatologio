<?php
session_start();
if (!isset($_SESSION['afm'])) {
    header("Location: index.php");
    exit;
}
include 'db.php';

$afm = $_SESSION['afm'];
$sqlOffers = "SELECT id, offerDate, description, hours, status FROM Offers WHERE customerId = (SELECT code FROM Customers WHERE afm = ?);";
$paramsOffers = array($afm);
$stmtOffers = sqlsrv_query($conn, $sqlOffers, $paramsOffers);
?>
<!DOCTYPE html>
<html lang="el">

<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Προσφορές</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.4.2/css/all.min.css">
    <link rel="stylesheet" href="style.css">
</head>

<body>
    <?php include 'navbar.php'; ?>
    <div class="container mt-5">
        <h2 class="text-center mb-4">Οι Προσφορές σας</h2>
        <div class="row">
            <?php while ($offer = sqlsrv_fetch_array($stmtOffers, SQLSRV_FETCH_ASSOC)): ?>
                <div class="col-md-4 d-flex mb-4">
                    <div class="card bank-card shadow-lg h-100 w-100 <?php echo $statusClass; ?>">
                        <div class="card-header bg-primary text-white bank-header">
                            <h5 class="mb-0">Προσφορά #<?php echo htmlspecialchars($offer['id']); ?></h5>
                        </div>
                        <div class="card-body">
                            <p><strong>Ημερομηνία:</strong> <?php echo $offer['offerDate']->format('d-m-Y'); ?></p>
                            <p><strong>Περιγραφή:</strong> <?php echo htmlspecialchars($offer['description']); ?></p>
                            <p><strong>Χρεώσιμες Ώρες:</strong> <?php echo htmlspecialchars($offer['hours']); ?></p>
                            <p><strong>Κατάσταση:</strong>
                                <span>
                                    <?php
                                    if (trim($offer['status']) == 'Απορρίψη') {
                                        echo '<i class="fas fa-times-circle text-danger"></i> Απόρριψη';
                                    } elseif (trim($offer['status']) == 'Αποδοχή') {
                                        echo '<i class="fas fa-check-circle text-success"></i> Αποδοχή';
                                    } else {
                                        echo '<i class="fas fa-hourglass-half text-warning"></i> Αναμονή';
                                    }
                                    ?>
                                </span>
                            </p>
                            <?php if (trim($offer['status']) == 'Αναμονή'): ?>
                                <form action="update_offer.php" method="post">
                                    <input type="hidden" name="offerId" value="<?php echo $offer['id']; ?>">
                                    <button type="submit" name="action" value="accept"
                                        class="btn btn-success btn-sm">Αποδοχή</button>
                                    <button type="submit" name="action" value="reject"
                                        class="btn btn-danger btn-sm">Απόρριψη</button>
                                </form>
                            <?php endif; ?>
                        </div>
                    </div>
                </div>
            <?php endwhile; ?>
        </div>
        <div class="text-center mt-4">
            <a href="dashboard.php" class="btn btn-secondary"><i class="fas fa-arrow-left"></i> Επιστροφή</a>
        </div>
    </div>
</body>

</html>