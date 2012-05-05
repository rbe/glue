// GORM
grails.gorm.failOnError = true

// Glue
glue {
	service {
		api {
			baseurl = "http://localhost:8080/gluews"
		}
		mail {
			defaultTo = ["support@bensmann.com"]
		}
	}
}
