USE [Pelatologio]
GO

/****** Object:  Table [dbo].[appointments]    Script Date: 17/12/2024 1:27:35 πμ ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[appointments](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[customerid] [int] NULL,
	[title] [nvarchar](255) NULL,
	[description] [nvarchar](max) NULL,
	[start_time] [datetime] NULL,
	[end_time] [datetime] NULL,
	[calendar_id] [int] NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY] TEXTIMAGE_ON [PRIMARY]
GO

ALTER TABLE [dbo].[appointments]  WITH CHECK ADD FOREIGN KEY([customerid])
REFERENCES [dbo].[Customers] ([code])
GO

ALTER TABLE [dbo].[appointments]  WITH CHECK ADD  CONSTRAINT [FK_appointments_calendars] FOREIGN KEY([calendar_id])
REFERENCES [dbo].[calendars] ([id])
GO

ALTER TABLE [dbo].[appointments] CHECK CONSTRAINT [FK_appointments_calendars]
GO


USE [Pelatologio]
GO

/****** Object:  Table [dbo].[calendars]    Script Date: 17/12/2024 1:27:47 πμ ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE TABLE [dbo].[calendars](
	[id] [int] IDENTITY(1,1) NOT NULL,
	[name] [nvarchar](255) NOT NULL,
	[color] [nvarchar](20) NOT NULL,
PRIMARY KEY CLUSTERED
(
	[id] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON) ON [PRIMARY]
) ON [PRIMARY]
GO

