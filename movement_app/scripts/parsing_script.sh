#!/bin/bash
 cat deployment.yaml | yq e 'del(.status)' - \
    | yq e 'del(.metadata.uid)' - \
    | yq e 'del(.metadata.generation)' - \
    | yq e 'del(.metadata.resourceVersion)' - \
    | yq e 'del(.metadata.selfLink)' - \
    | yq e 'del(.metadata.creationTimestamp)' - \
    | yq e 'del(.spec.template.metadata.creationTimestamp)' - \
    | yq e 'del(.spec.template.metadata.annotations."kubectl.kubernetes.io/restartedAt")' - \
    | yq e 'del(.metadata.annotations."deployment.kubernetes.io/revision")' - \
    | yq e 'del(.metadata.annotations."kubectl.kubernetes.io/last-applied-configuration")' - \
    > updated_deployment.yml