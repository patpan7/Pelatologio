<nav class="navbar navbar-expand-lg navbar-dark bg-primary">
    <div class="container">
        <a class="navbar-brand" href="dashboard.php">Portal Πελατών</a>
        <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
            <span class="navbar-toggler-icon"></span>
        </button>
        <div class="collapse navbar-collapse" id="navbarNav">
            <ul class="navbar-nav">
                <li class="nav-item">
                    <a class="nav-link <?= basename($_SERVER['PHP_SELF']) == 'dashboard.php' ? 'active' : '' ?>" href="dashboard.php">Αρχική</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= basename($_SERVER['PHP_SELF']) == 'offers.php' ? 'active' : '' ?>" href="offers.php">Προσφορές</a>
                </li>
                <li class="nav-item">
                    <a class="nav-link <?= basename($_SERVER['PHP_SELF']) == 'contracts.php' ? 'active' : '' ?>" href="contracts.php">Συμβόλαια</a>
                </li>
				<li class="nav-item">
                    <a class="nav-link <?= basename($_SERVER['PHP_SELF']) == 'bank_accounts.php' ? 'active' : '' ?>" href="bank_accounts.php">Τραπεζικοί Λογαριασμοί</a>
                </li>
            </ul>
            <a href="logout.php" class="btn btn-light ms-auto">Αποσύνδεση</a>
        </div>
    </div>
</nav>
