package org.easytech.pelatologio;

public class test {
    public static void main(String[] args) {
        // Ρυθμίσεις για SMTP διακομιστή (π.χ., Gmail)
        String host = "smtp.gmail.com";
        String port = "587";
        String username = "patelos942@gmail.com";
        String password = "dhgv bawk wqlw szsj"; // Χρησιμοποίησε κωδικό εφαρμογής για Gmail

        EmailSender emailSender = new EmailSender(host, port, username, password);

        // Αποστολή του email
        String recipient = "patpan7@gmail.com";
        String subject = "Test Email";
        String messageContent = "This is a test email sent from Java.";

        emailSender.sendEmail(recipient, subject, messageContent);
    }

//    USE [Pelatologio]
//    GO
//
//    /****** Object:  Table [dbo].[CustomerAddresses]    Script Date: 16/11/2024 12:24:49 πμ ******/
//    SET ANSI_NULLS ON
//            GO
//
//    SET QUOTED_IDENTIFIER ON
//            GO
//
//    CREATE TABLE [dbo].[CustomerAddresses](
//            [AddressID] [int] IDENTITY(1,1) NOT NULL,
//	[CustomerID] [int] NOT NULL,
//	[Address] [nvarchar](255) NULL,
//            [Town] [nvarchar](255) NULL,
//            [Postcode] [nvarchar](20) NULL,
//            [Store] [nvarchar](255) NULL,
//    PRIMARY KEY CLUSTERED
//            (
//	[AddressID] ASC
//            )WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
//            ) ON [PRIMARY]
//    GO
//
//    ALTER TABLE [dbo].[CustomerAddresses]  WITH CHECK ADD FOREIGN KEY([CustomerID])
//    REFERENCES [dbo].[Customers] ([code])
//    GO
//

}