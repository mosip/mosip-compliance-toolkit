# compliance-toolkit Batchjob

Helm chart for installing Pre-Registration Batchjob service.

## Install
```console
$ kubectl create namespace “compliance-toolkit
$ helm repo add mosip https://mosip.github.io
$ helm -n compliance-toolkit-batch-job install my-release mosip/compliance-toolkit-batch-job
```
