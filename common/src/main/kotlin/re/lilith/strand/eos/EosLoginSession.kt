package re.lilith.strand.eos

import re.lilith.strand.backend.MeResponse
import gg.sona.eos.common.ProductUserId

class EosLoginSession(val sessionToken: String, val productUserId: ProductUserId, val me: MeResponse)
