# GCP Iceberg 

##  Setup

__Create bucket__

```

gcloud storage buckets create gs://epsstan-bucket1

gcloud storage buckets describe gs://epsstan-bucket1
creation_time: 2023-09-10T17:45:10+0000
default_storage_class: STANDARD
location: US
location_type: multi-region
metageneration: 2
name: epsstan-bucket1
public_access_prevention: inherited
rpo: DEFAULT
storage_url: gs://epsstan-bucket1/
uniform_bucket_level_access: true
update_time: 2023-09-10T19:00:23+0000

```

__Create connection__
```
bq mk --connection --location=US --project_id=sky-225649-proj-c89517cd --connection_type=CLOUD_RESOURCE epsstan-connection

bq show --format prettyjson --connection --location=US --connection epsstan-connection
{
  "cloudResource": {
    "serviceAccountId": "bqcx-519986318301-q57x@gcp-sa-bigquery-condel.iam.gserviceaccount.com"
  },
  "creationTime": "1694367192091",
  "lastModifiedTime": "1694367192091",
  "name": "projects/519986318301/locations/us/connections/epsstan-connection"
}
```

__Setup IAM__

```
gcloud storage buckets add-iam-policy-binding gs://mybucket --member=serviceaccount:bqcx-519986318301-q57x@gcp-sa-bigquery-condel.iam.gserviceaccount.com --role=roles/storage.admin


gcloud storage buckets get-iam-policy gs://epsstan-bucket1
bindings:
- members:
  - serviceAccount:bqcx-519986318301-q57x@gcp-sa-bigquery-condel.iam.gserviceaccount.com
  role: roles/storage.admin
- members:
  - projectEditor:sky-225649-proj-c89517cd
  - projectOwner:sky-225649-proj-c89517cd
  role: roles/storage.legacyBucketOwner
- members:
  - projectViewer:sky-225649-proj-c89517cd
  role: roles/storage.legacyBucketReader
- members:
  - projectEditor:sky-225649-proj-c89517cd
  - projectOwner:sky-225649-proj-c89517cd
  role: roles/storage.legacyObjectOwner
- members:
  - projectViewer:sky-225649-proj-c89517cd
  role: roles/storage.legacyObjectReader
etag: CAI=

```

__Create table__

```
CREATE TABLE `sky-225649-proj-c89517cd.epsstan.biglake1` ( 
	a integer, 
	b string)
CLUSTER BY a
WITH CONNECTION `sky-225649-proj-c89517cd.US.epsstan-connection` 
OPTIONS (
	file_format = 'PARQUET',
	table_format = `ICEBERG`,
	storage_uri = 'gs://epsstan-bucket1/biglake1'
)
```

__Insert rows__
```

insert into `sky-225649-proj-c89517cd.epsstan.biglake1` values(1, 'one');
insert into `sky-225649-proj-c89517cd.epsstan.biglake1` values(2, 'two');
insert into `sky-225649-proj-c89517cd.epsstan.biglake1` values(3, 'three');
```

__GCS Data___

```
gcloud storage ls --recursive gs://epsstan-bucket1
gs://epsstan-bucket1/:

gs://epsstan-bucket1/biglake1/:

gs://epsstan-bucket1/biglake1/data/:
gs://epsstan-bucket1/biglake1/data/67e408a0-b865-4e82-b308-987e5a932c88-5ddc569b86625943-f-00000-of-00001.parquet
gs://epsstan-bucket1/biglake1/data/6a831537-dd45-4991-9165-e5f3d01fad06-7ba0783902f6bd8d-f-00000-of-00001.parquet
gs://epsstan-bucket1/biglake1/data/b911e7cd-4b7a-41d5-902c-7ea642ebca49-af94c5c805dea867-f-00000-of-00001.parquet
```


__GCS Metadata __
```
TODO
```

__BigLake Metastore__
```

Note : Run from Gcloud shell (when using with Skylab projects)


curl --header "Authorization: Bearer $(gcloud auth application-default print-access-token)" --header "Accept: application/json" https://biglake.googleapis.com/v1alpha1/projects/sky-225649-proj-c89517cd/locations/us/catalogs
{
  "catalogs": [
    {
      "name": "projects/519986318301/locations/us/catalogs/iceberg_catalog_2",
      "createTime": "2023-05-09T15:10:03.282310Z",
      "updateTime": "2023-05-09T15:10:03.282310Z"
    },
    {
      "name": "projects/519986318301/locations/us/catalogs/iceberg_catalog",
      "createTime": "2023-05-05T15:07:18.994437Z",
      "updateTime": "2023-05-05T15:07:18.994437Z"
    }
  ]
}

curl --header "Authorization: Bearer $(gcloud auth application-default print-access-token)" --header "Accept: application/json" https://biglake.googleapis.com/v1alpha1/projects/sky-225649-proj-c89517cd/locations/us/catalogs/iceberg_catalog/databases
{
  "databases": [
    {
      "name": "projects/519986318301/locations/us/catalogs/iceberg_catalog/databases/iceberg_warehouse",
      "createTime": "2023-05-05T15:07:19.558265Z",
      "updateTime": "2023-05-05T15:07:19.558265Z",
      "type": "HIVE",
      "hiveOptions": {
        "locationUri": "gs://gstest-bucket-1/iceberg_catalog/iceberg_warehouse.db",
        "parameters": {
          "owner": "spark"
        }
      }
    }
  ]
}

```

