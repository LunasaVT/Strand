package re.lilith.strand.backend

class BackendException(status: Int, val error: String) : RuntimeException("backend $status: $error")
