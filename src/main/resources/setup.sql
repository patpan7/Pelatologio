

CREATE TABLE [dbo].[Accountants](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NULL,
	[email] [nvarchar](255) NULL,
	[phone] [nvarchar](255) NULL,
	[mobile] [nvarchar](255) NULL,
	[erganiEmail] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Applications](
	[ApplicationID] [int] IDENTITY(1,1) NOT NULL,
	[ApplicationName] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([ApplicationID] ASC)
);
GO

CREATE TABLE [dbo].[ApplicationSteps](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[application_id] [int] NOT NULL,
	[step_name] [nvarchar](255) NOT NULL,
	[step_order] [int] NOT NULL,
	[action_type] [nvarchar](50) NULL,
	[action_config_json] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Appointments](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customerid] [int] NULL,
	[title] [nvarchar](255) NULL,
	[description] [nvarchar](max) NULL,
	[start_time] [datetime] NULL,
	[end_time] [datetime] NULL,
	[calendar_id] [int] NOT NULL,
	[completed] [bit] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Calendars](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[CallLogs](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customerId] [int] NULL,
	[callerNumber] [nvarchar](50) NULL,
	[callerName] [nvarchar](255) NULL,
	[callType] [nvarchar](20) NULL,
	[startTime] [datetime] NULL,
	[endTime] [datetime] NULL,
	[durationSeconds] [bigint] NULL,
	[appUser] [nvarchar](100) NULL,
	[notes] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Commissions](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[partner_id] [int] NOT NULL,
	[customer_id] [int] NOT NULL,
	[supplier_id] [int] NOT NULL,
	[rate] [decimal](5, 2) NOT NULL,
	[start_date] [date] NOT NULL,
	[end_date] [date] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[CourierTracking](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customerId] [int] NOT NULL,
	[tracking_number] [nvarchar](max) NOT NULL,
	[date] [datetime] NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[customer_anydesk_ids](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customer_id] [int] NOT NULL,
	[anydesk_id] [varchar](255) NOT NULL,
	[description] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[CustomerAddresses](
	[AddressID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NOT NULL,
	[Address] [nvarchar](255) NULL,
	[Town] [nvarchar](255) NULL,
	[Postcode] [nvarchar](20) NULL,
	[Store] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED ([AddressID] ASC)
);
GO

CREATE TABLE [dbo].[CustomerLogins](
	[LoginID] [int] IDENTITY(1,1) NOT NULL,
	[CustomerID] [int] NULL,
	[ApplicationID] [int] NULL,
	[Username] [nvarchar](255) NULL,
	[Password] [nvarchar](255) NULL,
	[Tag] [nvarchar](255) NULL,
	[phone] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED ([LoginID] ASC)
);
GO

CREATE TABLE [dbo].[CustomerMyPosDetails](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customer_id] [int] NOT NULL,
	[mypos_client_id] [nvarchar](50) NULL,
	[verification_status] [nvarchar](50) NULL,
	[account_status] [nvarchar](50) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[CustomerProjects](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customer_id] [int] NOT NULL,
	[application_id] [int] NOT NULL,
	[project_name] [nvarchar](255) NOT NULL,
	[start_date] [date] NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Customers](
	[code] [int] IDENTITY(1,1) NOT NULL,
	[name] [varchar](300) NULL,
	[title] [varchar](300) NULL,
	[job] [nvarchar](255) NULL,
	[afm] [nvarchar](20) NOT NULL,
	[phone1] [varchar](100) NULL,
	[phone2] [varchar](100) NULL,
	[mobile] [varchar](100) NULL,
	[address] [nvarchar](255) NULL,
	[town] [nvarchar](255) NULL,
	[email] [nvarchar](255) NULL,
	[manager] [nvarchar](255) NULL,
	[managerPhone] [nvarchar](255) NULL,
	[locked_by] [varchar](255) NULL,
	[postcode] [varchar](10) NULL,
	[notes] [nvarchar](max) NULL,
	[email2] [nvarchar](255) NULL,
	[accId] [int] NULL,
	[accName1] [varchar](300) NULL,
	[accEmail1] [nvarchar](255) NULL,
	[recommendation1] [varchar](300) NULL,
	[balance] [nchar](10) NULL,
	[balanceReason] [nvarchar](max) NULL,
	[isActive] [bit] NULL,
	[created_at] [datetime] NULL,
	[recommendation] [int] NULL,
	[subJobTeam] [int] NULL,
	[mypos_client_id] [varchar](50) NULL,
PRIMARY KEY CLUSTERED ([code] ASC)
);
GO

CREATE TABLE [dbo].[Devices](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[serial] [varchar](255) NOT NULL,
	[description] [nvarchar](max) NULL,
	[itemId] [int] NULL,
	[customerId] [int] NULL,
	[rate] [nchar](10) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Items](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[description] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[JobTeams](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Offers](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[offerDate] [datetime] NOT NULL,
	[description] [nvarchar](max) NULL,
	[hours] [nchar](10) NULL,
	[status] [nchar](20) NULL,
	[customerId] [int] NULL,
	[response_date] [datetime] NULL,
	[last_updated] [datetime] NULL,
	[offer_file_paths] [nvarchar](max) NULL,
	[sended] [nchar](20) NULL,
	[is_archived] [bit] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Orders](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[title] [varchar](255) NOT NULL,
	[description] [nvarchar](max) NULL,
	[dueDate] [datetime] NULL,
	[is_completed] [bit] NULL,
	[customerId] [int] NULL,
	[supplierId] [int] NULL,
	[is_ergent] [bit] NULL,
	[is_wait] [bit] NULL,
	[is_received] [bit] NULL,
	[is_delivered] [bit] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[PartnerEarnings](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[partner_id] [int] NOT NULL,
	[supplier_payment_id] [int] NOT NULL,
	[customer_id] [int] NOT NULL,
	[commission_id] [int] NOT NULL,
	[earning_date] [date] NOT NULL,
	[earning_amount] [decimal](10, 2) NOT NULL,
	[partner_invoice_status] [nvarchar](50) NOT NULL,
	[partner_invoice_ref] [nvarchar](100) NULL,
	[payment_to_partner_status] [nvarchar](50) NOT NULL,
	[payment_to_partner_date] [date] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Partners](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [varchar](300) NULL,
	[title] [varchar](300) NULL,
	[job] [nvarchar](255) NULL,
	[afm] [nvarchar](20) NOT NULL,
	[phone1] [varchar](100) NULL,
	[phone2] [varchar](100) NULL,
	[mobile] [varchar](100) NULL,
	[address] [nvarchar](255) NULL,
	[town] [nvarchar](255) NULL,
	[email] [nvarchar](255) NULL,
	[manager] [nvarchar](255) NULL,
	[managerPhone] [nvarchar](255) NULL,
	[locked_by] [varchar](255) NULL,
	[postcode] [varchar](10) NULL,
	[notes] [nvarchar](max) NULL,
	[email2] [nvarchar](255) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[ProjectStepProgress](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[project_id] [int] NOT NULL,
	[step_id] [int] NOT NULL,
	[is_completed] [bit] NOT NULL,
	[completion_date] [date] NULL,
	[notes] [nvarchar](max) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Recommendations](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[SimplySetupProgress](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[app_login_id] [int] NOT NULL,
	[stock] [bit] NULL,
	[register] [bit] NULL,
	[auth] [bit] NULL,
	[accept] [bit] NULL,
	[mail] [bit] NULL,
	[param] [bit] NULL,
	[mydata] [bit] NULL,
	[delivered] [bit] NULL,
	[paid] [bit] NULL,
	[years] [nvarchar](2) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[SubJobTeams](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[jobTeamId] [int] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[SubsCategories](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Subscriptions](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[title] [varchar](255) NOT NULL,
	[endDate] [datetime] NOT NULL,
	[note] [nvarchar](max) NULL,
	[subCatId] [int] NULL,
	[customerId] [int] NULL,
	[price] [nchar](10) NULL,
	[sended] [nchar](20) NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[SupplierPayments](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[supplier_id] [int] NOT NULL,
	[customer_id] [int] NOT NULL,
	[payment_date] [date] NOT NULL,
	[amount] [decimal](10, 2) NOT NULL,
	[description] [nvarchar](max) NULL,
	[is_calculated] [bit] NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Suppliers](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NULL,
	[title] [nvarchar](255) NULL,
	[phone] [nvarchar](255) NULL,
	[mobile] [nvarchar](255) NULL,
	[contact] [nvarchar](255) NULL,
	[email] [nvarchar](255) NULL,
	[site] [nvarchar](255) NULL,
	[afm] [nvarchar](10) NULL,
	[email2] [nvarchar](255) NULL,
	[notes] [nvarchar](max) NULL,
	[has_commissions] [bit] NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[TaskCategories](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

CREATE TABLE [dbo].[Tasks](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[title] [varchar](255) NOT NULL,
	[description] [nvarchar](max) NULL,
	[dueDate] [datetime] NULL,
	[is_completed] [bit] NULL,
	[customerId] [int] NULL,
	[category] [varchar](50) NULL,
	[is_ergent] [bit] NOT NULL,
	[is_wait] [bit] NULL,
	[is_calendar] [bit] NULL,
	[start_time] [datetime] NULL,
	[end_time] [datetime] NULL,
	[snooze] [bit] NULL,
PRIMARY KEY CLUSTERED ([id] ASC)
);
GO

-- Foreign Keys
ALTER TABLE [dbo].[ApplicationSteps]  WITH CHECK ADD  CONSTRAINT [FK_Steps_To_Applications] FOREIGN KEY([application_id])
REFERENCES [dbo].[Applications] ([ApplicationID])
ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[Appointments]  WITH CHECK ADD FOREIGN KEY([customerid])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[Appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_calendars] FOREIGN KEY([calendar_id])
REFERENCES [dbo].[Calendars] ([id]);
GO

ALTER TABLE [dbo].[CallLogs]  WITH CHECK ADD  CONSTRAINT [FK_CallLogs_Customers] FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Commissions]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[Commissions]  WITH CHECK ADD FOREIGN KEY([partner_id])
REFERENCES [dbo].[Partners] ([id]);
GO

ALTER TABLE [dbo].[Commissions]  WITH CHECK ADD FOREIGN KEY([supplier_id])
REFERENCES [dbo].[Suppliers] ([id]);
GO

ALTER TABLE [dbo].[CourierTracking]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[customer_anydesk_ids]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code])
ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[CustomerAddresses]  WITH CHECK ADD FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[CustomerLogins]  WITH CHECK ADD FOREIGN KEY([ApplicationID])
REFERENCES [dbo].[Applications] ([ApplicationID]);
GO

ALTER TABLE [dbo].[CustomerLogins]  WITH CHECK ADD FOREIGN KEY([CustomerID])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[CustomerMyPosDetails]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[CustomerProjects]  WITH CHECK ADD  CONSTRAINT [FK_Projects_To_Applications] FOREIGN KEY([application_id])
REFERENCES [dbo].[Applications] ([ApplicationID])
ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[CustomerProjects]  WITH CHECK ADD  CONSTRAINT [FK_Projects_To_Customers] FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code])
ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[Customers]  WITH CHECK ADD FOREIGN KEY([accId])
REFERENCES [dbo].[Accountants] ([id]);
GO

ALTER TABLE [dbo].[Customers]  WITH CHECK ADD  CONSTRAINT [FK_Customers_Recommendations] FOREIGN KEY([recommendation])
REFERENCES [dbo].[Recommendations] ([id])
ON UPDATE CASCADE
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Customers]  WITH CHECK ADD  CONSTRAINT [FK_Customers_SubJobTeams] FOREIGN KEY([subJobTeam])
REFERENCES [dbo].[SubJobTeams] ([id]);
GO

ALTER TABLE [dbo].[Devices]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Devices]  WITH CHECK ADD FOREIGN KEY([itemId])
REFERENCES [dbo].[Items] ([id])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Offers]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Orders]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Orders]  WITH CHECK ADD FOREIGN KEY([supplierId])
REFERENCES [dbo].[Suppliers] ([id])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[PartnerEarnings]  WITH CHECK ADD FOREIGN KEY([commission_id])
REFERENCES [dbo].[Commissions] ([id]);
GO

ALTER TABLE [dbo].[PartnerEarnings]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[PartnerEarnings]  WITH CHECK ADD FOREIGN KEY([partner_id])
REFERENCES [dbo].[Partners] ([id]);
GO

ALTER TABLE [dbo].[PartnerEarnings]  WITH CHECK ADD FOREIGN KEY([supplier_payment_id])
REFERENCES [dbo].[SupplierPayments] ([id]);
GO

ALTER TABLE [dbo].[ProjectStepProgress]  WITH CHECK ADD  CONSTRAINT [FK_Progress_To_Projects] FOREIGN KEY([project_id])
REFERENCES [dbo].[CustomerProjects] ([id])
ON DELETE CASCADE;
GO

ALTER TABLE [dbo].[ProjectStepProgress]  WITH CHECK ADD  CONSTRAINT [FK_Progress_To_Steps] FOREIGN KEY([step_id])
REFERENCES [dbo].[ApplicationSteps] ([id]);
GO

ALTER TABLE [dbo].[SimplySetupProgress]  WITH CHECK ADD FOREIGN KEY([app_login_id])
REFERENCES [dbo].[CustomerLogins] ([LoginID]);
GO

ALTER TABLE [dbo].[SubJobTeams]  WITH CHECK ADD FOREIGN KEY([jobTeamId])
REFERENCES [dbo].[JobTeams] ([id]);
GO

ALTER TABLE [dbo].[Subscriptions]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[Subscriptions]  WITH CHECK ADD FOREIGN KEY([subCatId])
REFERENCES [dbo].[SubsCategories] ([id])
ON DELETE SET NULL;
GO

ALTER TABLE [dbo].[SupplierPayments]  WITH CHECK ADD FOREIGN KEY([customer_id])
REFERENCES [dbo].[Customers] ([code]);
GO

ALTER TABLE [dbo].[SupplierPayments]  WITH CHECK ADD FOREIGN KEY([supplier_id])
REFERENCES [dbo].[Suppliers] ([id]);
GO

ALTER TABLE [dbo].[Tasks]  WITH CHECK ADD FOREIGN KEY([customerId])
REFERENCES [dbo].[Customers] ([code])
ON DELETE SET NULL;
GO

-- Insert Initial Data
SET IDENTITY_INSERT [dbo].[Accountants] ON;
INSERT [dbo].[Accountants] ([id], [name], [email], [phone], [mobile], [erganiEmail]) VALUES (0, 'Χωρίς Λογιστή', '', '', '', '');
SET IDENTITY_INSERT [dbo].[Accountants] OFF;
GO

SET IDENTITY_INSERT [dbo].[Applications] ON;
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (1, 'MyPos');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (2, 'Simply');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (3, 'Taxis');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (4, 'Emblem');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (5, 'Εργάνη');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (6, 'Πελατολόγιο');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (7, 'NinePOS');
INSERT [dbo].[Applications] ([ApplicationID], [ApplicationName]) VALUES (8, 'EDPS');
SET IDENTITY_INSERT [dbo].[Applications] OFF;
GO

SET IDENTITY_INSERT [dbo].[Customers] ON;
INSERT [dbo].[Customers] ([code], [name], [afm], [isActive]) VALUES (0, 'Χωρίς Πελάτη', '000000000', 1);
SET IDENTITY_INSERT [dbo].[Customers] OFF;
GO

SET IDENTITY_INSERT [dbo].[JobTeams] ON;
INSERT [dbo].[JobTeams] ([id], [name]) VALUES (0, 'Χωρίς Ομάδα');
SET IDENTITY_INSERT [dbo].[JobTeams] OFF;
GO

SET IDENTITY_INSERT [dbo].[Recommendations] ON;
INSERT [dbo].[Recommendations] ([id], [name]) VALUES (0, 'Χωρίς σύσταση');
SET IDENTITY_INSERT [dbo].[Recommendations] OFF;
GO

SET IDENTITY_INSERT [dbo].[SubJobTeams] ON;
INSERT [dbo].[SubJobTeams] ([id], [name], [jobTeamId]) VALUES (0, 'Χωρίς υποομάδα', 0);
SET IDENTITY_INSERT [dbo].[SubJobTeams] OFF;
GO

SET IDENTITY_INSERT [dbo].[Suppliers] ON;
INSERT [dbo].[Suppliers] ([id], [name], [has_commissions]) VALUES (0, 'Χωρίς Προμηθευτή', 0);
SET IDENTITY_INSERT [dbo].[Suppliers] OFF;
GO
