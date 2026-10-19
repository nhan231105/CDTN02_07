// API Configuration
export const API_BASE_URL = 'http://localhost:8082/api';

export const API_ENDPOINTS = {
  // Auth
  AUTH_REGISTER: '/auth/register',
  AUTH_LOGIN: '/auth/login',
  AUTH_HEALTH: '/auth/health',
  AUTH_ADMIN_STATS: '/auth/admin/stats',
  AUTH_SEARCH: '/auth/admin/search',

  // Jobs
  JOBS_LIST: '/jobs',
  JOBS_CREATE: '/jobs',
  JOBS_GET: (id) => `/jobs/${id}`,
  JOBS_UPDATE: (id) => `/jobs/${id}`,
  JOBS_DELETE: (id) => `/jobs/${id}`,
  JOBS_SEARCH: '/jobs/search',
  JOBS_EMPLOYER: (employerId) => `/jobs/employer/${employerId}`,

  // Applications
  APPLICATIONS_CREATE: (jobId) => `/jobs/${jobId}/applications`,
  APPLICATIONS_LIST: (jobId) => `/jobs/${jobId}/applications`,
  APPLICATIONS_APPROVE: (jobId, appId) => `/jobs/${jobId}/applications/${appId}/approve`,
  APPLICATIONS_REJECT: (jobId, appId) => `/jobs/${jobId}/applications/${appId}/reject`,

  // Reviews
  REVIEWS_CREATE: '/reviews',
  REVIEWS_BY_JOB: '/reviews',
  REVIEWS_BY_USER: (userId) => `/reviews/user/${userId}`,

  // Notifications
  NOTIFICATIONS_LIST: '/notifications',
  NOTIFICATIONS_READ: (id) => `/notifications/${id}/read`,
  NOTIFICATIONS_UNREAD: '/notifications/unread',

  // Profiles
  STUDENT_PROFILE_GET: (userId) => `/profiles/student/${userId}`,
  STUDENT_PROFILE_UPDATE: (userId) => `/profiles/student/${userId}`,
  EMPLOYER_PROFILE_GET: (userId) => `/profiles/employer/${userId}`,
  EMPLOYER_PROFILE_UPDATE: (userId) => `/profiles/employer/${userId}`,

  // Admin
  ADMIN_JOBS_DELETE: (jobId) => `/admin/jobs/${jobId}`,
  ADMIN_USERS_LOCK: (userId) => `/admin/users/${userId}/lock`,
  ADMIN_USERS_UNLOCK: (userId) => `/admin/users/${userId}/unlock`,
  ADMIN_VIOLATIONS_LIST: '/admin/violations',
  ADMIN_VIOLATIONS_RESOLVE: (violationId) => `/admin/violations/${violationId}/resolve`,
};

// API Service class
class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async request(method, endpoint, data = null, params = null) {
    try {
      const url = new URL(this.baseURL + endpoint);
      
      // Add query parameters if provided
      if (params) {
        Object.keys(params).forEach(key => {
          url.searchParams.append(key, params[key]);
        });
      }

      const options = {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
      };

      // Add body for POST, PATCH, PUT requests
      if (data && (method === 'POST' || method === 'PATCH' || method === 'PUT')) {
        options.body = JSON.stringify(data);
      }

      const response = await fetch(url.toString(), options);
      const result = await response.json();

      if (!response.ok) {
        throw new Error(result.message || `HTTP error! status: ${response.status}`);
      }

      return result;
    } catch (error) {
      console.error('API Error:', error);
      throw error;
    }
  }

  // Auth endpoints
  register(userData) {
    return this.request('POST', API_ENDPOINTS.AUTH_REGISTER, userData);
  }

  login(credentials) {
    return this.request('POST', API_ENDPOINTS.AUTH_LOGIN, credentials);
  }

  getHealthStatus() {
    return this.request('GET', API_ENDPOINTS.AUTH_HEALTH);
  }

  getAdminStats() {
    return this.request('GET', API_ENDPOINTS.AUTH_ADMIN_STATS);
  }

  // Jobs endpoints
  getJobs(params = null) {
    return this.request('GET', API_ENDPOINTS.JOBS_LIST, null, params);
  }

  getJob(id) {
    return this.request('GET', API_ENDPOINTS.JOBS_GET(id));
  }

  createJob(jobData) {
    return this.request('POST', API_ENDPOINTS.JOBS_CREATE, jobData);
  }

  updateJob(id, jobData) {
    return this.request('PATCH', API_ENDPOINTS.JOBS_UPDATE(id), jobData);
  }

  searchJobs(params) {
    return this.request('GET', API_ENDPOINTS.JOBS_SEARCH, null, params);
  }

  getEmployerJobs(employerId) {
    return this.request('GET', API_ENDPOINTS.JOBS_EMPLOYER(employerId));
  }

  // Applications endpoints
  applyForJob(jobId, applicationData) {
    return this.request('POST', API_ENDPOINTS.APPLICATIONS_CREATE(jobId), applicationData);
  }

  getJobApplications(jobId, employerId) {
    return this.request('GET', API_ENDPOINTS.APPLICATIONS_LIST(jobId), null, { employerId });
  }

  approveApplication(jobId, appId, employerId) {
    return this.request('PATCH', API_ENDPOINTS.APPLICATIONS_APPROVE(jobId, appId), null, { employerId });
  }

  rejectApplication(jobId, appId, employerId) {
    return this.request('PATCH', API_ENDPOINTS.APPLICATIONS_REJECT(jobId, appId), null, { employerId });
  }

  // Reviews endpoints
  createReview(reviewData) {
    return this.request('POST', API_ENDPOINTS.REVIEWS_CREATE, reviewData);
  }

  getReviewsByJob(jobId) {
    return this.request('GET', API_ENDPOINTS.REVIEWS_BY_JOB, null, { jobId });
  }

  getReviewsByUser(userId) {
    return this.request('GET', API_ENDPOINTS.REVIEWS_BY_USER(userId));
  }

  // Notifications endpoints
  getNotifications(userId, params = null) {
    const allParams = { userId, ...(params || {}) };
    return this.request('GET', API_ENDPOINTS.NOTIFICATIONS_LIST, null, allParams);
  }

  markNotificationAsRead(notificationId) {
    return this.request('PATCH', API_ENDPOINTS.NOTIFICATIONS_READ(notificationId));
  }

  getUnreadCount(userId) {
    return this.request('GET', API_ENDPOINTS.NOTIFICATIONS_UNREAD, null, { userId });
  }

  // Profiles endpoints
  getStudentProfile(userId) {
    return this.request('GET', API_ENDPOINTS.STUDENT_PROFILE_GET(userId));
  }

  updateStudentProfile(userId, profileData) {
    return this.request('PATCH', API_ENDPOINTS.STUDENT_PROFILE_UPDATE(userId), profileData);
  }

  getEmployerProfile(userId) {
    return this.request('GET', API_ENDPOINTS.EMPLOYER_PROFILE_GET(userId));
  }

  updateEmployerProfile(userId, profileData) {
    return this.request('PATCH', API_ENDPOINTS.EMPLOYER_PROFILE_UPDATE(userId), profileData);
  }
}

export default new ApiService();
